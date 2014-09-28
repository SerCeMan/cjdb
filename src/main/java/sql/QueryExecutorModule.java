package sql;

import dagger.Module;
import dagger.Provides;

/**
 * @author Sergey Tselovalnikov
 * @since 29.09.14
 */
@Module(injects = QueryExecutor.class)
public class QueryExecutorModule {
    @Provides
    public QueryExecutor provideQueryExecutor() {
        return new QueryExecutorImpl();
    }
}
