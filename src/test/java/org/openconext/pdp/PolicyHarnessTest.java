package org.openconext.pdp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openconext.pdp.model.PdpPolicyDefinition;
import org.openconext.pdp.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PolicyHarnessTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PolicyService policyService;

    @BeforeEach
    protected void beforeEach() {
        RestAssured.port = port;
    }

    @TestFactory
    Stream<DynamicTest> policyHarness() throws Exception {
        String policy = System.getProperty("policy");
        return Stream.of(Objects.requireNonNull(new ClassPathResource("test-harness").getFile().listFiles()))
            .filter(File::isDirectory)
            .filter(file -> policy == null || file.getName().equalsIgnoreCase(policy))
            .map(directory -> DynamicTest.dynamicTest(
                "Policy harness: " + directory.getName(),
                () -> testPolicy(directory)
            ));
    }

    @SneakyThrows
    private void testPolicy(File policyDirectory) {
        boolean record = Boolean.parseBoolean(System.getProperty("record", "false"));
        if (record) {
            throw new IllegalStateException("record mode is not supported in pdp-ng");
        }
        List<File> files = List.of(Objects.requireNonNull(policyDirectory.listFiles()));
        String request = readFile(files, "request.json");
        File responseFile = files.stream()
            .filter(file -> file.getName().equalsIgnoreCase("response.json"))
            .findFirst().orElseThrow();
        Map<String, Object> responseMap = objectMapper.readValue(new FileInputStream(responseFile), new TypeReference<>() {
        });

        List<PdpPolicyDefinition> policies = files.stream()
            .filter(file -> file.getName().toLowerCase().startsWith("policy"))
            .filter(file -> file.getName().toLowerCase().endsWith(".json"))
            .map(this::readPolicy)
            .toList();

        policyService.replacePolicies(policies);
        policyService.reload();

        Map<String, Object> result = given()
            .auth().preemptive().basic("pdp", "secret")
            .when()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(request)
            .post("/pdp/api/manage/decide")
            .as(new TypeRef<>() {
            });

        if (!responseMap.equals(result)) {
            ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
            String expected = objectWriter.writeValueAsString(responseMap);
            String actual = objectWriter.writeValueAsString(result);
            List<String> expectedLines = expected.lines().toList();
            List<String> actualLines = actual.lines().toList();
            Patch<String> patch = DiffUtils.diff(expectedLines, actualLines);
            List<String> unifiedDiff = UnifiedDiffUtils
                .generateUnifiedDiff("expected", "actual", expectedLines, patch, 3);
            String message = """
                Response did not match expected JSON
                ===== Expected =====
                %s
                ===== Actual   =====
                %s
                ===== Unified Diff (expected vs actual) =====
                %s
                """.formatted(
                expected,
                actual,
                String.join("\n", unifiedDiff)
            );
            fail(message);
        }
    }

    @SneakyThrows
    private PdpPolicyDefinition readPolicy(File file) {
        return objectMapper.readValue(new FileInputStream(file), PdpPolicyDefinition.class);
    }

    private String readFile(List<File> files, String name) throws IOException {
        return Files.readString(files.stream()
            .filter(file -> file.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseThrow().toPath(), Charset.defaultCharset());
    }

    private Path resolveWritableResponsePath(Path responsePath) {
        return responsePath;
    }
}
