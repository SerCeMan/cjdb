package ru.cjdb.scheme.storage;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.dto.Metainfo;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.*;
import java.io.*;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public class MetaStorageImpl implements MetaStorage {
    static final String METAINFO_FILE = "metainfo.xml";

    private final String fileName;

    @Inject
    public MetaStorageImpl(ConfigStorage configStorage) {
        fileName = configStorage.getRootPath() + METAINFO_FILE;
    }

    @Override
    public Metainfo getMetainfo() {
        File file = new File(fileName);
        if (!file.exists()) {
            return new Metainfo();
        }
        try (InputStream os = new BufferedInputStream(new FileInputStream(fileName))) {
            JAXBContext context = JAXBContext.newInstance(Metainfo.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (Metainfo) unmarshaller.unmarshal(os);
        } catch (JAXBException | IOException e) {
            throw new RuntimeException("Error loading metainfo.xml:", e);
        }
    }

    @Override
    public void saveMetainfo(Metainfo metainfo) {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(fileName))) {
            JAXBContext context = JAXBContext.newInstance(Metainfo.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(metainfo, os);
        } catch (Exception e) {
            throw new RuntimeException("Error saving metainfo.xml:", e);
        }
    }
}