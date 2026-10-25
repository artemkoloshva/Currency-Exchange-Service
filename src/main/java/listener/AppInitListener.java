package listener;

import com.zaxxer.hikari.HikariDataSource;
import dao.*;
import db.DatabaseInitializer;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServlet;
import mapper.*;
import org.mapstruct.factory.Mappers;
import parser.BigDecimalParser;
import service.*;
import servlet.*;
import util.DataSourceFactory;
import validator.CurrencyValidator;
import validator.ExchangeRateValidator;

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

        BigDecimalParser bigDecimalParser = new BigDecimalParser();

        CurrencyValidator currencyValidator = new CurrencyValidator();
        ExchangeRateValidator exchangeRateValidator = new ExchangeRateValidator(currencyValidator, bigDecimalParser);

        CurrencyService currencyService = new CurrencyServiceImpl(currencyDao, currencyMapper);
        ExchangeRateService exchangeRateService = new ExchangeRateServiceImpl(exchangeRateDao, exchangeRateMapper);
        ExchangeService exchangeService = new ExchangeServiceImpl(exchangeRateDao, currencyMapper);

        register(context, "currencies", new CurrenciesServlet(
                currencyService, currencyValidator), "/currencies");
        register(context, "currency", new CurrencyServlet(
                currencyService, currencyValidator), "/currency/*");
        register(context, "exchangeRates", new ExchangeRatesServlet(
                exchangeRateService, exchangeRateValidator, bigDecimalParser), "/exchangeRates");
        register(context, "exchangeRate", new ExchangeRateServlet(
                exchangeRateService, exchangeRateValidator, bigDecimalParser), "/exchangeRate/*");
        register(context, "exchange", new ExchangeServlet(
                exchangeService, currencyValidator, exchangeRateValidator, bigDecimalParser), "/exchange");
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
