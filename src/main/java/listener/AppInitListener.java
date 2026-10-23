package listener;

import com.zaxxer.hikari.HikariDataSource;
import dao.*;
import db.DatabaseInitializer;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServlet;
import service.*;
import servlet.*;
import util.DataSourceFactory;

@WebListener
public class AppInitListener implements ServletContextListener {
    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        dataSource = DataSourceFactory.create();
        new DatabaseInitializer(dataSource).initialize();

        CurrencyService currencyService = new DefaultCurrencyService(new JdbcCurrencyDao(dataSource));
        ExchangeService exchangeService = new DefaultExchangeService(new JdbcExchangeRateDao(dataSource));

        register(ctx, "currencies", new CurrenciesServlet(currencyService), "/currencies");
        register(ctx, "currency", new CurrencyServlet(currencyService), "/currency/*");
        register(ctx, "exchangeRates", new ExchangeRatesServlet(exchangeService), "/exchangeRates");
        register(ctx, "exchangeRate", new ExchangeRateServlet(exchangeService), "/exchangeRate/*");
        register(ctx, "exchange", new ExchangeServlet(exchangeService), "/exchange");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private void register(ServletContext ctx, String name, HttpServlet servlet, String mapping) {
        ctx.addServlet(name, servlet).addMapping(mapping);
    }
}
