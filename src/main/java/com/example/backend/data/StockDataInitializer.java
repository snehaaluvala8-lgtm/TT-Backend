package com.example.backend.data;

import com.example.backend.model.Stock;
import com.example.backend.repository.StockRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StockDataInitializer implements CommandLineRunner {

	private static final List<StockSeed> INITIAL_STOCKS = List.of(
		new StockSeed("RELIANCE", "Reliance Industries", "1450.00"),
		new StockSeed("TCS", "Tata Consultancy Services", "3200.00"),
		new StockSeed("HDFCBANK", "HDFC Bank", "1680.00"),
		new StockSeed("ICICIBANK", "ICICI Bank", "1180.00"),
		new StockSeed("INFY", "Infosys", "1550.00"),
		new StockSeed("ITC", "ITC Limited", "430.00"),
		new StockSeed("SBIN", "State Bank of India", "780.00"),
		new StockSeed("BHARTIARTL", "Bharti Airtel", "1720.00"),
		new StockSeed("LT", "Larsen & Toubro", "3600.00"),
		new StockSeed("HINDUNILVR", "Hindustan Unilever", "2450.00"),
		new StockSeed("AXISBANK", "Axis Bank", "1120.00"),
		new StockSeed("KOTAKBANK", "Kotak Mahindra Bank", "1780.00"),
		new StockSeed("MARUTI", "Maruti Suzuki India", "12500.00"),
		new StockSeed("SUNPHARMA", "Sun Pharmaceutical Industries", "1750.00"),
		new StockSeed("TATAMOTORS", "Tata Motors", "720.00"),
		new StockSeed("TATASTEEL", "Tata Steel", "155.00"),
		new StockSeed("WIPRO", "Wipro", "290.00"),
		new StockSeed("HCLTECH", "HCL Technologies", "1580.00"),
		new StockSeed("ASIANPAINT", "Asian Paints", "2350.00"),
		new StockSeed("NTPC", "NTPC Limited", "360.00"),
		new StockSeed("POWERGRID", "Power Grid Corporation of India", "310.00"),
		new StockSeed("ONGC", "Oil and Natural Gas Corporation", "270.00"),
		new StockSeed("ADANIENT", "Adani Enterprises", "2450.00"),
		new StockSeed("ADANIPORTS", "Adani Ports and Special Economic Zone", "1320.00"),
		new StockSeed("BAJFINANCE", "Bajaj Finance", "7200.00"),
		new StockSeed("TITAN", "Titan Company", "3450.00"),
		new StockSeed("ULTRACEMCO", "UltraTech Cement", "10800.00"),
		new StockSeed("NESTLEIND", "Nestle India", "2250.00"),
		new StockSeed("TECHM", "Tech Mahindra", "1580.00"),
		new StockSeed("JSWSTEEL", "JSW Steel", "980.00")
	);

	private final StockRepository stockRepository;

	public StockDataInitializer(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	@Override
	public void run(String... args) {
		Set<String> existingSymbols = stockRepository.findAll().stream()
			.map(stock -> stock.getSymbol().toUpperCase(Locale.ROOT))
			.collect(Collectors.toSet());

		List<Stock> missingStocks = INITIAL_STOCKS.stream()
			.filter(seed -> !existingSymbols.contains(seed.symbol()))
			.map(seed -> new Stock(seed.symbol(), seed.companyName(), new BigDecimal(seed.price())))
			.toList();

		if (!missingStocks.isEmpty()) {
			stockRepository.saveAll(missingStocks);
		}
	}

	private record StockSeed(String symbol, String companyName, String price) {
	}
}
