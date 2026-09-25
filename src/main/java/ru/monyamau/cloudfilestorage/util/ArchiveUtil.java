package ru.monyamau.cloudfilestorage.util;

import lombok.experimental.UtilityClass;
import ru.monyamau.cloudfilestorage.domain.ResourceItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@UtilityClass
public final class ArchiveUtil {
    public void archiveItemsToZip(List<ResourceItem> resourceItemList, OutputStream outputStream, String path, Function<String, InputStream> downloader) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (ResourceItem resourceItem : resourceItemList) {
                String fullObjectName = resourceItem.objectName();
                if (fullObjectName.equals(path)) continue;
                if (resourceItem.isDir()) {
                    zipOutputStream.putNextEntry(new ZipEntry(fullObjectName.substring(path.length())));
                    zipOutputStream.closeEntry();
                    continue;
                }
                zipOutputStream.putNextEntry(new ZipEntry(fullObjectName.substring(path.length())));
                try (InputStream inputStream = downloader.apply(resourceItem.objectName())) {
                    inputStream.transferTo(zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
        }
    }
}
