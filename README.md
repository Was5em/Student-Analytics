# 🔐 SecureFile Encryptor

> A security-focused utility built with Java to protect sensitive files using advanced encryption algorithms.  
> Built as part of the **CARETECH** technical portfolio.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔒 AES-256 Encryption | Military-grade encryption for any file type |
| 🔑 Password Protection | User-defined passphrase — only you can decrypt |
| ✅ File Integrity | SHA-256 hash verification to detect tampering |
| 📁 Any File Type | Encrypts PDFs, images, docs, databases — everything |
| 🖥️ Simple GUI | Clean Swing interface, no command line needed |

---

## 🛠️ Tech Stack

- **Java SE 17+** — Core logic
- **JCA (Java Cryptography Architecture)** — Built-in, no external crypto libs
- **AES-256-CBC** — Encryption algorithm
- **PBKDF2WithHmacSHA256** — Password-based key derivation
- **SHA-256** — File integrity hashing
- **Swing** — Desktop GUI
- **Maven** — Build tool

---

## 🚀 Getting Started

### Prerequisites
- Java JDK 17+
- Maven 3.8+

### Run
```bash
git clone https://github.com/YOUR_USERNAME/SecureFile-Java-Encryptor.git
cd SecureFile-Java-Encryptor
mvn compile
mvn exec:java -Dexec.mainClass="securefile.Main"
```

---

## 📁 Project Structure

```
SecureFile-Java-Encryptor/
├── src/securefile/
│   ├── Main.java                  # Entry point + Swing GUI
│   ├── crypto/
│   │   ├── AESEncryptor.java      # AES-256-CBC encrypt/decrypt
│   │   ├── KeyDerivation.java     # PBKDF2 key from password
│   │   └── FileHasher.java        # SHA-256 integrity check
│   ├── model/
│   │   └── EncryptedFile.java     # Metadata model
│   ├── controller/
│   │   └── EncryptorController.java
│   └── util/
│       └── FileUtil.java
├── pom.xml
└── README.md
```

---

## 🔐 How It Works

```
Password  ──► PBKDF2 ──► 256-bit Key ──►  AES-CBC  ──► Encrypted File
                                    Salt + IV stored in file header
```

1. User provides a **password** and selects a **file**
2. A **random salt** and **IV** are generated
3. PBKDF2 derives a **256-bit AES key** from the password + salt
4. AES-CBC **encrypts** the file content
5. Salt + IV + SHA-256 hash are stored in the output file **header**
6. To decrypt: password + encrypted file → original file restored

---

## 👨‍💻 Author

**CARETECH** — Built with ❤️ for secure data management.
