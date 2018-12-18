package pivotal.io.quotesgatewauy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.util.Map;


@SpringBootApplication
public class QuotesGatewauyApplication {

	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("path_to_query", r -> r.path("/{ticker}")
						.filters(f -> f.setPath("/v1/quotes")
								.filter(pathToParamGatewayFilterFactory().apply("")))
						.uri("https://quotes-wise-badger.cfapps.io/"))
				.build();
	}

	@Bean
	public PathToParamGatewayFilterFactory pathToParamGatewayFilterFactory() {
		return new PathToParamGatewayFilterFactory();
	}

	static class PathToParamGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

		@Override
		public GatewayFilter apply(Object config) {
			return (exchange, chain) -> {

				Map<String, String> uriVariables = ServerWebExchangeUtils.getUriTemplateVariables(exchange);

				if (uriVariables.containsKey("ticker")) {
					String ticker = uriVariables.get("ticker");
					URI uri = exchange.getRequest().getURI();
					URI newUri = UriComponentsBuilder.fromUri(uri)
							.replaceQuery("q="+ticker)
							.build(true)
							.toUri();

					ServerHttpRequest request = exchange.getRequest().mutate().uri(newUri).build();
					return chain.filter(exchange.mutate().request(request).build());
				}

				return chain.filter(exchange);
			};
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(QuotesGatewauyApplication.class, args);
	}
}