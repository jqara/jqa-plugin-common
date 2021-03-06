package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.shared.io.FileNameNormalizer;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scanner plugin for instances of {@link File}.
 */
public class FileScannerPlugin extends AbstractResourceScannerPlugin<File, FileDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerPlugin.class);

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return !item.isDirectory();
    }

    @Override
    public FileDescriptor scan(final File file, String path, Scope scope, Scanner scanner) throws IOException {
        String normalizedPath = FileNameNormalizer.normalize(path);
        LOGGER.debug("Scanning '{}'.", normalizedPath);
        try (FileResource fileResource = new RealFileResource(file);) {
            return scanner.scan(fileResource, normalizedPath, scope);
        }
    }

    private static class RealFileResource implements FileResource {
        private final File file;

        public RealFileResource(File file) {
            this.file = file;
        }

        @Override
        public InputStream createStream() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public File getFile() {
            return file;
        }

        @Override
        public void close() {
        }


        @Override
        public String toString() {
            return file.toString();
        }
    }
}
