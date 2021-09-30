package com.devrev.apirest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.devrev.apirest.ApplicationContextLoad;
import com.devrev.apirest.model.Usuario;
import com.devrev.apirest.repository.UsuarioRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAutenticacaoService {

	// Tempo de validade do Token de 1 dia
	private static final long EXPIRATION_TIME = 86400000;
	
	// Uma senha única para compor a autenticação
	private static final String SECRET = "SenhaSecreta---*-*xs-x*s-x*-sx*-s";
	
	// Prefixo padrão de Token
	private static final String TOKEN_PREFIX = "Bearer";
	
	// Prefixo para o header da resposta
	private static final String HEADER_STRING = "Authorization";
	
	// Gerando token de autenticado e adicionando ao cabeçalho de resposta Http
	public void addAuthentication(HttpServletResponse response, String username) throws IOException {
	
		// Montagem do token
		
						// Chama o gerador de Token
		String JWT = Jwts.builder()
						// Adiciona um usuário
					.setSubject(username)
						// Seta um limite de tempo até deslogar o usuário
					.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
						// Adiciona um algoritmo pra criptografar a token e geração da senha
					.signWith(SignatureAlgorithm.HS512, SECRET).compact();
		
		// Concatena o token com o prefixo
		String token = TOKEN_PREFIX + " " + JWT; // Bearer SenhaSecreta---*-*xs-x*s-x*-sx*-s
		
		// Adiciona no cabeçalho HTTP
		response.addHeader(HEADER_STRING, token); // Authorization: Bearer SenhaSecreta---*-*xs-x*s-x*-sx*-s
		
		// Escreve o token como resposta no corpo HTTP
		response.getWriter().write("{\"Authorization\": \""+token+"\"}");
	}
	
	// Retorna o usuário validado com token ou caso não seja válido retorna null
	public Authentication getAuthentication(HttpServletRequest request) {
		
		// Pega o token enviado no cabeçalho HTTP
		String token = request.getHeader(HEADER_STRING);
		
		// Se existir token
		if(token != null) {
			
			// Faz a validação do token do usuário na requisição
			String user = Jwts.parser()
					// Coloca a assinatura como SenhaSecreta---*-*xs-x*s-x*-sx*-s
					.setSigningKey(SECRET)
					// Tira o prefixo do token
					.parseClaimsJws(token.replace(TOKEN_PREFIX, "")) // SenhaSecreta---*-*xs-x*s-x*-sx*-s
					// Pega o corpo da requisição
					.getBody()
					// Pega o objeto da requisição
					.getSubject(); // Retorna o usuário ex: João Silva
			
			// Verifica se o usuario existe
			if(user != null) {
				
				// Cria uma instância do usuário logado
				Usuario usuario = ApplicationContextLoad.getApplicationContext()
								  .getBean(UsuarioRepository.class).findUserByLogin(user);
				
				// Verificar se encontrou o usuário no token
					if(usuario != null) {
						
						// Retorna o usuário autenticado
						return new UsernamePasswordAuthenticationToken(usuario.getLogin(),
																	   usuario.getSenha(),
																	   usuario.getAuthorities()
																	   );
				}
			}
		}
		return null; // Não existe token validado
	}
	
}
