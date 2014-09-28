import config.ConfigStorage;
import config.Props;
import dagger.ObjectGraph;
import sql.DataSet;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
@Singleton
public class CjDB implements CjDataBase {

    private ConfigStorage configStorage;

    @Inject
    public CjDB(ConfigStorage configStorage) {
        this.configStorage = configStorage;
    }

    public static void main(String[] args) {
        ObjectGraph objectGraph = ObjectGraph.create(new CjDbModule());
        CjDataBase db = objectGraph.get(CjDB.class);
        System.out.println(db.exec(Props.PATH));
    }

    @Override
    public String execPrint(String sql) {
        return configStorage.getProperty(sql);
    }

    @Override
    public DataSet exec(String sql) {
        return null;
    }
}
