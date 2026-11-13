package org.delcom.ifs25042springbootcashflow.adapter.storage;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {

    @Test
    void storesFilesAndResolvesPaths() throws Exception {
        Path root = Files.createTempDirectory("cashflow-storage-");
        FileStorage storage = new FileStorage(root.toString());

        String stored = storage.store(
                "proofs",
                "receipt.PNG",
                new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));

        assertTrue(stored.startsWith("proofs/"));
        assertTrue(stored.endsWith(".png"));
        assertEquals("content", Files.readString(storage.resolve(stored)));
        assertEquals(root.toAbsolutePath().normalize(), storage.root());
        assertTrue(storage.store(
                "proofs",
                "no-extension",
                new ByteArrayInputStream(new byte[] {1})).endsWith(".bin"));
        assertTrue(storage.store(
                "proofs",
                null,
                new ByteArrayInputStream(new byte[] {1})).endsWith(".bin"));
        assertTrue(storage.store(
                "proofs",
                "very-long-extension.abcdefghijk",
                new ByteArrayInputStream(new byte[] {1})).endsWith(".bin"));
    }
}