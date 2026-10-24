package listener;

import com.zaxxer.hikari.HikariDataSource;
import dao.*;
import db.DatabaseInitializer;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServlet;
import mapper.*;
import org.mapstruct.factory.Mappers;
import service.*;
import servlet.*;
import util.DataSourceFactory;

@WebListener
public class AppInitListener implements ServletContextListener {
    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();

        dataSource = DataSourceFactory.create();
        new DatabaseInitializer(dataSource).initialize();

        CurrencyDao currencyDao = new JdbcCurrencyDao(dataSource);
        ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao(dataSource);

        CurrencyMapper currencyMapper = Mappers.getMapper(CurrencyMapper.class);
        ExchangeRateMapper exchangeRateMapper = Mappers.getMapper(ExchangeRateMapper.class);

        CurrencyService currencyService = new CurrencyServiceImpl(currencyDao, currencyMapper);
        ExchangeRateService exchangeRateService = new ExchangeRateServiceImpl(exchangeRateDao, exchangeRateMapper);
        ExchangeService exchangeService = new ExchangeServiceImpl(exchangeRateDao, currencyMapper);

        register(context, "currencies", new CurrenciesServlet(currencyService), "/currencies");
        register(context, "currency", new CurrencyServlet(currencyService), "/currency/*");
        register(context, "exchangeRates", new ExchangeRatesServlet(exchangeService), "/exchangeRates");
        register(context, "exchangeRate", new ExchangeRateServlet(exchangeService), "/exchangeRate/*");
        register(context, "exchange", new ExchangeServlet(exchangeService), "/exchange");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private void register(ServletContext context, String name, HttpServlet servlet, String mapping) {
        context.addServlet(name, servlet).addMapping(mapping);
    }
}
