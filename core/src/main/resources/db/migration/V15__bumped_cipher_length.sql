ALTER TABLE cipher_store
    MODIFY cipher_standard BLOB NULL;

UPDATE cipher_store SET cipher_standard = cipher_long
    WHERE cipher_long IS NOT NULL AND LENGTH(cipher_long) <= 65535;

UPDATE cipher_store SET cipher_long = NULL
    WHERE cipher_standard IS NOT NULL;
