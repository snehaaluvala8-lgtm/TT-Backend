package com.example.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(
	name = "holdings",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_holding_user_stock",
		columnNames = {"user_id", "stock_id"}
	)
)
public class Holding {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_holding_user"))
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stock_id", nullable = false, foreignKey = @ForeignKey(name = "fk_holding_stock"))
	private Stock stock;

	@Column(nullable = false)
	private long quantity;

	@Column(name = "average_price", nullable = false, precision = 19, scale = 4)
	private BigDecimal averagePrice;

	@Version
	private Long version;

	protected Holding() {
	}

	public Holding(AppUser user, Stock stock, long quantity, BigDecimal averagePrice) {
		this.user = user;
		this.stock = stock;
		this.quantity = quantity;
		this.averagePrice = averagePrice;
	}

	public Long getId() {
		return id;
	}

	public AppUser getUser() {
		return user;
	}

	public Stock getStock() {
		return stock;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(BigDecimal averagePrice) {
		this.averagePrice = averagePrice;
	}

	public Long getVersion() {
		return version;
	}
}
