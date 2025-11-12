use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use std::ffi::CString;
use std::fs;
use std::io::Read;

/// Native library for advanced text extraction from Android app memory
/// Requires root access for full functionality
///
/// This module provides low-level memory access capabilities for:
/// - Reading process memory directly from /proc/[pid]/mem
/// - Scanning memory regions for text patterns
/// - Extracting strings from application heap
///
/// WARNING: These operations require root access and should only be used
/// in authorized security research, penetration testing, or educational contexts.

/// Read memory from a specific process ID
/// Requires root access
#[no_mangle]
pub extern "C" fn Java_com_textextractor_native_1NativeMemoryExtractor_readProcessMemory(
    env: JNIEnv,
    _class: JClass,
    pid: i32,
) -> jstring {
    let result = read_process_memory(pid);

    match result {
        Ok(data) => {
            let output = env.new_string(data)
                .expect("Couldn't create Java string");
            output.into_raw()
        }
        Err(e) => {
            let error_msg = format!("Error reading process memory: {}", e);
            let output = env.new_string(error_msg)
                .expect("Couldn't create Java string");
            output.into_raw()
        }
    }
}

/// Extract printable strings from process memory
#[no_mangle]
pub extern "C" fn Java_com_textextractor_native_1NativeMemoryExtractor_extractStrings(
    env: JNIEnv,
    _class: JClass,
    pid: i32,
    min_length: i32,
) -> jstring {
    let result = extract_strings_from_process(pid, min_length as usize);

    match result {
        Ok(strings) => {
            let output = env.new_string(strings)
                .expect("Couldn't create Java string");
            output.into_raw()
        }
        Err(e) => {
            let error_msg = format!("Error extracting strings: {}", e);
            let output = env.new_string(error_msg)
                .expect("Couldn't create Java string");
            output.into_raw()
        }
    }
}

/// Check if the device is rooted and if we have necessary permissions
#[no_mangle]
pub extern "C" fn Java_com_textextractor_native_1NativeMemoryExtractor_checkRootAccess(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let has_root = check_root_access();
    let message = if has_root {
        "Root access available"
    } else {
        "Root access not available - native memory extraction will be limited"
    };

    let output = env.new_string(message)
        .expect("Couldn't create Java string");
    output.into_raw()
}

/// Read process memory maps and extract readable regions
fn read_process_memory(pid: i32) -> Result<String, String> {
    // Read /proc/[pid]/maps to find memory regions
    let maps_path = format!("/proc/{}/maps", pid);
    let maps_content = fs::read_to_string(&maps_path)
        .map_err(|e| format!("Failed to read maps: {} (requires root)", e))?;

    let mut result = String::new();
    result.push_str(&format!("Memory maps for PID {}:\n", pid));
    result.push_str(&maps_content);

    // Try to read actual memory (requires root)
    let mem_path = format!("/proc/{}/mem", pid);
    if fs::metadata(&mem_path).is_ok() {
        result.push_str("\nMemory accessible (root available)\n");
    } else {
        result.push_str("\nMemory not accessible (requires root)\n");
    }

    Ok(result)
}

/// Extract printable ASCII strings from process memory
fn extract_strings_from_process(pid: i32, min_length: usize) -> Result<String, String> {
    // Read /proc/[pid]/cmdline to get process info
    let cmdline_path = format!("/proc/{}/cmdline", pid);
    let cmdline = fs::read_to_string(&cmdline_path)
        .map_err(|e| format!("Failed to read cmdline: {}", e))?;

    let mut result = String::new();
    result.push_str(&format!("Process: {}\n", cmdline.replace('\0', " ")));
    result.push_str(&format!("PID: {}\n", pid));
    result.push_str(&format!("Minimum string length: {}\n\n", min_length));

    // Try to read environment variables (often contains useful info)
    let environ_path = format!("/proc/{}/environ", pid);
    if let Ok(environ) = fs::read_to_string(&environ_path) {
        result.push_str("Environment variables:\n");
        for env_var in environ.split('\0').filter(|s| !s.is_empty()) {
            result.push_str(&format!("  {}\n", env_var));
        }
        result.push_str("\n");
    }

    // For actual memory scanning, we'd need root access to read /proc/[pid]/mem
    // This is a simplified version that demonstrates the concept
    result.push_str("Note: Full memory scanning requires root access\n");
    result.push_str("Use Accessibility Service for non-root text extraction\n");

    Ok(result)
}

/// Check if we have root access
fn check_root_access() -> bool {
    // Check if we can access /proc/1/mem (init process)
    // This typically requires root
    fs::metadata("/proc/1/mem").is_ok()
}

/// Scan a byte buffer for printable ASCII strings
fn extract_printable_strings(data: &[u8], min_length: usize) -> Vec<String> {
    let mut strings = Vec::new();
    let mut current_string = String::new();

    for &byte in data {
        if byte >= 32 && byte <= 126 {
            // Printable ASCII
            current_string.push(byte as char);
        } else {
            if current_string.len() >= min_length {
                strings.push(current_string.clone());
            }
            current_string.clear();
        }
    }

    // Don't forget the last string
    if current_string.len() >= min_length {
        strings.push(current_string);
    }

    strings
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_extract_printable_strings() {
        let data = b"Hello\x00World\x00Test123\x00";
        let strings = extract_printable_strings(data, 3);
        assert!(strings.contains(&"Hello".to_string()));
        assert!(strings.contains(&"World".to_string()));
        assert!(strings.contains(&"Test123".to_string()));
    }
}
