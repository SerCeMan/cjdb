import dagger.Module;
import sql.QueryExecutorModule;
import sql.parser.QueryParserModule;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
@Module(
        injects = { CjDB.class },
        includes = { QueryParserModule.class, QueryExecutorModule.class}
)
public class CjDbModule {

}
