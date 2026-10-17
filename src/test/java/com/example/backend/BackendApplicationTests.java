package com.example.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:candlecraft-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
	"app.cors.allowed-origins=http://localhost:5173"
})
@AutoConfigureMockMvc
class BackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void userCanRegisterTradeAndSeeOnlyTheirOwnTransactions() throws Exception {
		String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		String username = "learner" + suffix;
		String password = "training-password";

		mockMvc.perform(get("/api/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
		mockMvc.perform(get("/api/stocks"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(30));
		mockMvc.perform(get("/api/portfolio"))
			.andExpect(status().isUnauthorized());

		MvcResult registration = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"username", username,
					"email", username + "@example.test",
					"password", password))))
			.andExpect(status().isCreated())
			.andReturn();
		String token = readToken(registration);
		String authorization = "Bearer " + token;

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"identifier", username + "@example.test",
					"password", password))))
			.andExpect(status().isOk());
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"username", username,
					"email", username + "@example.test",
					"password", password))))
			.andExpect(status().isConflict());

		mockMvc.perform(get("/api/portfolio").header("Authorization", authorization))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.availableBalance").value(50000.00))
			.andExpect(jsonPath("$.holdings.length()").value(0));

		mockMvc.perform(post("/api/trades/buy")
				.header("Authorization", authorization)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("symbol", "RELIANCE", "quantity", 0))))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/trades/buy")
				.header("Authorization", authorization)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("symbol", "RELIANCE", "quantity", 2))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalValue").value(2900.00));

		mockMvc.perform(post("/api/trades/buy")
				.header("Authorization", authorization)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("symbol", "RELIANCE", "quantity", 100000))))
			.andExpect(status().isConflict());

		mockMvc.perform(post("/api/trades/sell")
				.header("Authorization", authorization)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("symbol", "RELIANCE", "quantity", 1))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.availableBalance").value(48550.00));

		mockMvc.perform(get("/api/portfolio").header("Authorization", authorization))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.availableBalance").value(48550.00))
			.andExpect(jsonPath("$.holdings[0].quantity").value(1));
		mockMvc.perform(get("/api/transactions").header("Authorization", authorization))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));

		MvcResult secondRegistration = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"username", "other" + suffix,
					"email", "other" + suffix + "@example.test",
					"password", password))))
			.andExpect(status().isCreated())
			.andReturn();
		mockMvc.perform(get("/api/transactions").header("Authorization", "Bearer " + readToken(secondRegistration)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	private String readToken(MvcResult result) throws Exception {
		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
		return response.get("accessToken").stringValue();
	}
}
