# Contributing to Android Text Extractor

Thank you for your interest in contributing! This document provides guidelines for contributing to the project.

## Code of Conduct

- Be respectful and professional
- This tool is for educational and authorized security research only
- Do not submit features that enable malicious use
- Report security vulnerabilities responsibly

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/yourusername/Android-extract.git`
3. Create a feature branch: `git checkout -b feature-name`
4. Make your changes
5. Test thoroughly
6. Submit a pull request

## Development Setup

### Prerequisites

- Android Studio Arctic Fox or newer
- JDK 17
- Android SDK (API 24+)
- (Optional) Rust toolchain for native development

### Building

```bash
./gradlew assembleDebug
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest
```

## Coding Standards

### Kotlin Code

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Maximum line length: 120 characters
- Use 4 spaces for indentation

```kotlin
/**
 * Extracts text from accessibility node
 *
 * @param node The accessibility node to process
 * @param packageName Package name of the source app
 * @return List of extracted text strings
 */
fun extractTextFromNode(node: AccessibilityNodeInfo, packageName: String): List<String> {
    // Implementation
}
```

### Rust Code

- Follow [Rust API guidelines](https://rust-lang.github.io/api-guidelines/)
- Use `rustfmt` for formatting: `cargo fmt`
- Run clippy: `cargo clippy`
- Add documentation comments for public functions

```rust
/// Extracts printable strings from a byte buffer
///
/// # Arguments
/// * `data` - The byte buffer to scan
/// * `min_length` - Minimum string length to extract
///
/// # Returns
/// Vector of extracted strings
pub fn extract_printable_strings(data: &[u8], min_length: usize) -> Vec<String> {
    // Implementation
}
```

### Android Best Practices

- Follow [Android app architecture guidelines](https://developer.android.com/topic/architecture)
- Use lifecycle-aware components
- Handle configuration changes properly
- Implement proper error handling
- Add null safety checks
- Use resource files for all user-facing strings

## Testing Guidelines

### Required Tests

- Unit tests for business logic
- Integration tests for service interaction
- UI tests for critical user flows

### Test Coverage

- Aim for >70% code coverage for new features
- Test edge cases and error conditions
- Mock external dependencies

### Example Test

```kotlin
@Test
fun testTextExtraction() {
    val data = ExtractedTextData(
        packageName = "com.test",
        appName = "Test App",
        text = "Sample text",
        className = "TextView",
        viewIdResourceName = "test_id",
        eventType = "TextChanged"
    )

    TextDataRepository.addExtractedText(data)

    val retrieved = TextDataRepository.getAllData()
    assertEquals(1, retrieved.size)
    assertEquals("Sample text", retrieved[0].text)
}
```

## Pull Request Process

1. **Update Documentation**: Update README.md if adding features
2. **Add Tests**: Include tests for new functionality
3. **Update Changelog**: Add entry describing changes
4. **Code Review**: Address review feedback promptly
5. **Squash Commits**: Clean up commit history before merging

### PR Title Format

```
[Feature/Fix/Docs] Brief description

Example:
[Feature] Add export to CSV functionality
[Fix] Resolve crash on Android 12
[Docs] Update native library build instructions
```

### PR Description Template

```markdown
## Description
Brief description of changes

## Motivation
Why is this change needed?

## Changes Made
- Change 1
- Change 2

## Testing
How was this tested?

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Code follows project style guidelines
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Tested on physical device
```

## Feature Requests

### Before Submitting

- Check if feature already exists
- Search existing issues and PRs
- Consider if feature aligns with project goals

### Feature Request Template

```markdown
**Feature Description**
Clear description of the feature

**Use Case**
How would this feature be used?

**Proposed Implementation**
(Optional) How might this work?

**Alternatives Considered**
Other approaches you've considered
```

## Bug Reports

### Include

- Android version
- Device model
- App version
- Steps to reproduce
- Expected vs actual behavior
- Logcat output (if applicable)
- Screenshots/videos

### Template

```markdown
**Description**
Brief bug description

**Steps to Reproduce**
1. Step 1
2. Step 2
3. Step 3

**Expected Behavior**
What should happen

**Actual Behavior**
What actually happens

**Environment**
- Android version:
- Device:
- App version:

**Logcat**
```
Paste relevant logs here
```

**Screenshots**
Add if applicable
```

## Security

### Reporting Vulnerabilities

**Do not** open public issues for security vulnerabilities.

Instead:
1. Email security concerns privately
2. Include detailed description
3. Provide steps to reproduce
4. Allow reasonable time for fix before disclosure

### Security Considerations

- Never log sensitive data
- Validate all inputs
- Handle permissions properly
- Clear sensitive data from memory
- Follow Android security best practices

## Documentation

### Code Comments

- Explain **why**, not **what**
- Use KDoc/Rustdoc for public APIs
- Keep comments up-to-date

### README Updates

- Update features list for new functionality
- Add examples for new APIs
- Update installation/build steps if changed
- Keep screenshots current

## Commit Messages

### Format

```
<type>: <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Test additions/changes
- `chore`: Build process, dependencies

### Examples

```
feat: Add CSV export functionality

Implements CSV export for extracted text data.
Includes timestamp, app name, and text fields.

Closes #123

---

fix: Resolve crash on Android 12

AccessibilityService was crashing on API 31+ due to
missing null check on node.getChild().

Fixes #456

---

docs: Update native library build instructions

Add troubleshooting section for common NDK issues.
Include macOS-specific instructions.
```

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

## Questions?

- Open an issue for general questions
- Check existing documentation
- Review closed issues/PRs for similar questions

Thank you for contributing to Android Text Extractor!
