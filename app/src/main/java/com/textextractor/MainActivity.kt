package com.textextractor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import java.io.File
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var enableServiceButton: Button
    private lateinit var clearLogButton: Button
    private lateinit var exportLogButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TextLogAdapter

    private val textDataListener: (ExtractedTextData) -> Unit = { data ->
        runOnUiThread {
            adapter.addItem(data)
            recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        updateServiceStatus()

        // Register listener for new extracted text
        TextDataRepository.addListener(textDataListener)
    }

    private fun initViews() {
        statusTextView = findViewById(R.id.statusTextView)
        enableServiceButton = findViewById(R.id.enableServiceButton)
        clearLogButton = findViewById(R.id.clearLogButton)
        exportLogButton = findViewById(R.id.exportLogButton)
        recyclerView = findViewById(R.id.recyclerView)

        enableServiceButton.setOnClickListener {
            openAccessibilitySettings()
        }

        clearLogButton.setOnClickListener {
            clearLog()
        }

        exportLogButton.setOnClickListener {
            exportLog()
        }
    }

    private fun setupRecyclerView() {
        adapter = TextLogAdapter(TextDataRepository.getAllData().toMutableList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun updateServiceStatus() {
        val isEnabled = isAccessibilityServiceEnabled()
        statusTextView.text = if (isEnabled) {
            getString(R.string.service_running)
        } else {
            getString(R.string.service_not_running)
        }

        enableServiceButton.visibility = if (isEnabled) View.GONE else View.VISIBLE
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${packageName}/${TextExtractionAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service) == true
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun clearLog() {
        TextDataRepository.clearData()
        adapter.clearItems()
        Timber.d("Log cleared")
    }

    private fun exportLog() {
        try {
            val data = TextDataRepository.getAllData()
            val exportDir = File(getExternalFilesDir(null), "exports")
            exportDir.mkdirs()

            val timestamp = System.currentTimeMillis()
            val exportFile = File(exportDir, "text_extract_$timestamp.txt")

            FileWriter(exportFile).use { writer ->
                writer.write("Text Extraction Log Export\n")
                writer.write("Generated: ${java.util.Date(timestamp)}\n")
                writer.write("Total entries: ${data.size}\n")
                writer.write("=" .repeat(50) + "\n\n")

                data.forEach { item ->
                    writer.write(item.toLogString())
                    writer.write("\n\n")
                }
            }

            Timber.d("Log exported to: ${exportFile.absolutePath}")

            // Show a simple toast-like message
            statusTextView.text = "Exported to: ${exportFile.absolutePath}"
        } catch (e: Exception) {
            Timber.e(e, "Error exporting log")
            statusTextView.text = "Export failed: ${e.message}"
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        TextDataRepository.removeListener(textDataListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                updateServiceStatus()
                adapter.setItems(TextDataRepository.getAllData())
                true
            }
            R.id.action_settings -> {
                openAccessibilitySettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

/**
 * RecyclerView adapter for displaying extracted text data
 */
class TextLogAdapter(private val items: MutableList<ExtractedTextData>) :
    RecyclerView.Adapter<TextLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appNameTextView: TextView = view.findViewById(R.id.appNameTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
        val extractedTextView: TextView = view.findViewById(R.id.extractedTextView)
        val detailsTextView: TextView = view.findViewById(R.id.detailsTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_extracted_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.appNameTextView.text = "${item.appName} (${item.packageName})"
        holder.timestampTextView.text = item.getFormattedTimestamp()
        holder.extractedTextView.text = item.text

        val details = buildString {
            append("Event: ${item.eventType}")
            item.className?.let { append("\nClass: $it") }
            item.viewIdResourceName?.let { append("\nView ID: $it") }
        }
        holder.detailsTextView.text = details
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: ExtractedTextData) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    fun setItems(newItems: List<ExtractedTextData>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
