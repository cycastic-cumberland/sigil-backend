# Sigil

**Backend**. [Frontend](https://github.com/cycastic-cumberland/sigil-frontend.git).

**Sigil** is a zero-trust, cryptographically enforced file storage
system designed to protect sensitive data from both external threats
and internal compromise—including administrative abuse.

---

## Core Features

- End-to-end encrypted file storage using per-partition and per-user keys
- Zero-trust design: administrators can't decrypt your data
- Fine-grained access control at the partition level
- Multi-tenant structure with isolated namespaces
- Optional server-assisted encryption to mitigate client key leaks

## Roadmap

TBA

## Installation

Clone the repo

```bash
git clone https://github.com/cycastic-cumberland/sigil-backend.git
cd sigil-backend
```

Run the repo

```bash
./mvnw spring-boot:run
```

## License

See [LICENSE.txt](LICENSE.txt).
