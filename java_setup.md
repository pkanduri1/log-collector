# Setting up Java 21 on macOS

Since your system uses Homebrew (detected from your current Java 17 installation), the easiest way to upgrade is via `brew`.

## Option 1: Using Homebrew (Recommended)

1.  **Search for available versions**:
    ```bash
    brew search openjdk
    ```

2.  **Install Java 21**:
    ```bash
    brew install openjdk@21
    ```

3.  **Link the version** (Important):
    Homebrew installs JDKs as "keg-only" to avoid conflicts. You need to symlink it for the system to see it.
    
    **For Apple Silicon (M1/M2/M3):**
    ```bash
    sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk
    ```
    
    **For Intel Macs:**
    ```bash
    sudo ln -sfn /usr/local/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk
    ```

4.  **Verify**:
    Open a new terminal or source your shell profile, then run:
    ```bash
    java -version
    # Output should include: openjdk version "21.0.2" ...
    ```

## Option 2: Using SDKMAN! (Flexible)

If you switch between Java versions often (e.g., 17 for legacy, 21 for this project), use SDKMAN.

1.  **Install SDKMAN**:
    ```bash
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    ```

2.  **Install Java 21**:
    ```bash
    sdk install java 21.0.2-open
    ```

3.  **Switch Versions**:
    ```bash
    sdk use java 21.0.2-open
    ```
