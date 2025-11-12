package com.textextractor

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import java.io.File
import java.io.FileWriter

/**
 * MainActivity - Main UI for text extraction app
 *
 * Implements IoC pattern with production and test constructors
 * Provides:
 * - Display of extracted text
 * - Multi-select and merge functionality
 * - Clipboard copy support
 * - Export to file
 */
class MainActivity(
    // IoC constructor for testing - allows dependency injection
    private val textMerger: TextMerger = TextMerger(),
    private val dataRepository: ITextDataRepository = TextDataRepository,
    private val clipboardHelper: ClipboardHelper? = null,
    private val appListProvider: AppListProvider? = null
) : AppCompatActivity() {

    // Production constructor - used by Android framework
    constructor() : this(
        textMerger = TextMerger(),
        dataRepository = TextDataRepository,
        clipboardHelper = null,  // Will be initialized in onCreate
        appListProvider = null  // Will be initialized in onCreate
    )

    private lateinit var statusTextView: TextView
    private lateinit var enableServiceButton: Button
    private lateinit var clearLogButton: Button
    private lateinit var exportLogButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var mergeControlsLayout: LinearLayout
    private lateinit var selectionCountTextView: TextView
    private lateinit var mergeButton: Button
    private lateinit var copyMergedButton: Button
    private lateinit var appFilterSpinner: Spinner
    private lateinit var adapter: TextLogAdapter
    private lateinit var clipboard: ClipboardHelper
    private lateinit var appProvider: AppListProvider

    private var selectedPackageFilter: String? = null

    private val textDataListener: (ExtractedTextData) -> Unit = { data ->
        runOnUiThread {
            adapter.addItem(data)
            recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize clipboard helper (for production use)
        clipboard = clipboardHelper ?: ClipboardHelper(this)
        appProvider = appListProvider ?: AppListProvider(this, dataRepository)

        initViews()
        setupRecyclerView()
        setupAppFilterSpinner()
        updateServiceStatus()

        // Register listener for new extracted text
        dataRepository.addListener(textDataListener)
    }

    private fun initViews() {
        statusTextView = findViewById(R.id.statusTextView)
        enableServiceButton = findViewById(R.id.enableServiceButton)
        clearLogButton = findViewById(R.id.clearLogButton)
        exportLogButton = findViewById(R.id.exportLogButton)
        recyclerView = findViewById(R.id.recyclerView)
        mergeControlsLayout = findViewById(R.id.mergeControlsLayout)
        selectionCountTextView = findViewById(R.id.selectionCountTextView)
        mergeButton = findViewById(R.id.mergeButton)
        copyMergedButton = findViewById(R.id.copyMergedButton)
        appFilterSpinner = findViewById(R.id.appFilterSpinner)

        enableServiceButton.setOnClickListener {
            openAccessibilitySettings()
        }

        clearLogButton.setOnClickListener {
            clearLog()
        }

        exportLogButton.setOnClickListener {
            exportLog()
        }

        mergeButton.setOnClickListener {
            showMergedText()
        }

        copyMergedButton.setOnClickListener {
            copyMergedToClipboard()
        }
    }

    private fun setupRecyclerView() {
        adapter = TextLogAdapter(
            items = getFilteredData().toMutableList(),
            textMerger = textMerger,
            onSelectionChanged = { updateMergeControls() }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupAppFilterSpinner() {
        // Get running apps + recently used apps from extraction history
        val runningApps = appProvider.getRunningApps()
        val recentApps = appProvider.getRecentlyUsedApps()

        // Combine and deduplicate
        val apps = mutableListOf(AppInfo("", getString(R.string.all_apps), false))
        apps.addAll(
            (runningApps + recentApps)
                .distinctBy { it.packageName }
                .sortedBy { it.appName.lowercase() }
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            apps
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        appFilterSpinner.adapter = adapter

        appFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedApp = apps[position]
                selectedPackageFilter = if (selectedApp.packageName.isEmpty()) null else selectedApp.packageName
                refreshDisplayedData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPackageFilter = null
                refreshDisplayedData()
            }
        }
    }

    private fun getFilteredData(): List<ExtractedTextData> {
        return AppFilter.filterByApp(dataRepository.getAllData(), selectedPackageFilter)
    }

    private fun refreshDisplayedData() {
        adapter.setItems(getFilteredData())
        textMerger.clearSelection()
        updateMergeControls()
    }

    private fun updateMergeControls() {
        val selectionCount = textMerger.getSelectionCount()

        if (selectionCount > 0) {
            mergeControlsLayout.visibility = View.VISIBLE
            selectionCountTextView.text = getString(R.string.selection_count, selectionCount)
        } else {
            mergeControlsLayout.visibility = View.GONE
        }
    }

    private fun showMergedText() {
        val mergedText = textMerger.getMergedSelection()

        if (mergedText.isEmpty()) {
            Toast.makeText(this, R.string.select_items_first, Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.merged_text_dialog_title)
            .setMessage(mergedText)
            .setPositiveButton("Copy") { _, _ ->
                clipboard.copyToClipboard(mergedText)
                Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun copyMergedToClipboard() {
        val mergedText = textMerger.getMergedSelection()

        if (mergedText.isEmpty()) {
            Toast.makeText(this, R.string.select_items_first, Toast.LENGTH_SHORT).show()
            return
        }

        clipboard.copyToClipboard(mergedText)
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
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
        dataRepository.clearData()
        adapter.clearItems()
        textMerger.clearSelection()
        updateMergeControls()
        Timber.d("Log cleared")
    }

    private fun exportLog() {
        try {
            val data = dataRepository.getAllData()
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
        dataRepository.removeListener(textDataListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                updateServiceStatus()
                refreshDisplayedData()
                setupAppFilterSpinner()  // Refresh app list
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
 * Interface for TextDataRepository to enable testing
 * Follows Dependency Inversion Principle
 */
interface ITextDataRepository {
    fun addExtractedText(data: ExtractedTextData)
    fun getAllData(): List<ExtractedTextData>
    fun clearData()
    fun addListener(listener: (ExtractedTextData) -> Unit)
    fun removeListener(listener: (ExtractedTextData) -> Unit)
    fun getDataByPackage(packageName: String): List<ExtractedTextData>
}

/**
 * RecyclerView adapter for displaying extracted text data
 * Supports multi-select for merging
 *
 * IoC constructor pattern for testability
 */
class TextLogAdapter(
    private val items: MutableList<ExtractedTextData>,
    private val textMerger: TextMerger = TextMerger(),  // IoC for testing
    private val onSelectionChanged: () -> Unit = {}
) : RecyclerView.Adapter<TextLogAdapter.ViewHolder>() {

    // Production constructor
    constructor(items: MutableList<ExtractedTextData>) : this(
        items = items,
        textMerger = TextMerger(),
        onSelectionChanged = {}
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.selectionCheckBox)
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

        // Handle selection checkbox
        holder.checkBox.isChecked = textMerger.isSelected(item)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textMerger.append(item)
            } else {
                textMerger.toggleSelection(item)
            }
            onSelectionChanged()
        }

        // Allow clicking the whole item to toggle selection
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
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

/**
 * Interface for TextDataRepository to enable testing
 * Follows Dependency Inversion Principle
 */
interface ITextDataRepository {
    fun addExtractedText(data: ExtractedTextData)
    fun getAllData(): List<ExtractedTextData>
    fun clearData()
    fun addListener(listener: (ExtractedTextData) -> Unit)
    fun removeListener(listener: (ExtractedTextData) -> Unit)
    fun getDataByPackage(packageName: String): List<ExtractedTextData>
}
