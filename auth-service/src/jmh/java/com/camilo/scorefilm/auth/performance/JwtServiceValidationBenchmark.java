package com.camilo.scorefilm.auth.performance;

import com.camilo.scorefilm.auth.auth.security.JwtService;
import com.camilo.scorefilm.auth.config.JwtProperties;
import com.camilo.scorefilm.auth.config.RsaKeyProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
@Threads(1)
public class JwtServiceValidationBenchmark {

    private JwtService jwtService;
    private UserDetails userDetails;
    private String preGeneratedToken;

    @Setup(Level.Trial)
    public void setUp() throws Exception {
        KeyPair keyPair = generateKeyPair();

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setKeyId("benchmark-key-id");
        jwtProperties.setExpiration(900_000L);

        RsaKeyProvider rsaKeyProvider = new InMemoryRsaKeyProvider(
                keyPair.getPrivate(),
                (RSAPublicKey) keyPair.getPublic()
        );

        jwtService = new JwtService(jwtProperties, rsaKeyProvider);
        userDetails = User.withUsername("benchmark-user")
                .password("benchmark-password")
                .authorities("ROLE_USER")
                .build();

        preGeneratedToken = jwtService.generateToken(userDetails);
    }

    @Benchmark
    public String generateToken() {
        return jwtService.generateToken(userDetails);
    }

    @Benchmark
    public boolean validateToken() {
        return jwtService.isTokenValid(preGeneratedToken, userDetails);
    }

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static final class InMemoryRsaKeyProvider extends RsaKeyProvider {

        private final PrivateKey privateKey;
        private final RSAPublicKey publicKey;

        private InMemoryRsaKeyProvider(PrivateKey privateKey, RSAPublicKey publicKey) {
            super(new JwtProperties());
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        @Override
        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        @Override
        public RSAPublicKey getPublicKey() {
            return publicKey;
        }
    }
}
