package ru.monyamau.cloudfilestorage.util;

import lombok.experimental.UtilityClass;
import ru.monyamau.cloudfilestorage.domain.ResourceItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@UtilityClass
public final class ArchiveUtil {
    public ByteArrayOutputStream archiveItemsToZip(List<ResourceItem> resourceItemList, String path, Function<String, InputStream> downloader) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        for (ResourceItem resourceItem : resourceItemList) {
            String fullObjectName = resourceItem.objectName();
            if (resourceItem.isDir()) continue;
            zipOutputStream.putNextEntry(new ZipEntry(fullObjectName.replace(path, "")));
            try (InputStream inputStream = downloader.apply(resourceItem.objectName())) {
                inputStream.transferTo(zipOutputStream);
            }
            zipOutputStream.closeEntry();
        }
        zipOutputStream.close();
        return byteArrayOutputStream;
    }
}
