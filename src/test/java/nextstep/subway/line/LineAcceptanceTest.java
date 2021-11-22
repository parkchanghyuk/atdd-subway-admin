package nextstep.subway.line;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    public static final String LINES_PATH = "/lines";

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        //given
        LineRequest 화곡 = 지하철_노선_정보("화곡역", "purple");

        // when
        ExtractableResponse<Response> 화곡역 = 지하철_노선_생성_요청(화곡);

        // then
        지하철_노선_생성됨(화곡역);
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLine2() {
        // given
        // 지하철_노선_등록되어_있음
        LineRequest 화곡 = 지하철_노선_정보("화곡", "purple");
        ExtractableResponse<Response> 화곡역 = 지하철_노선_생성_요청(화곡);

        // when
        ExtractableResponse<Response> 응답 = 지하철_노선_생성_요청(화곡);

        // then
        지하철_노선_생성_실패됨(응답);
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        // 지하철_노선_등록되어_있음
        LineRequest 화곡 = 지하철_노선_정보("화곡", "purple");
        ExtractableResponse<Response> 화곡역 = 지하철_노선_생성_요청(화곡);
        // 지하철_노선_등록되어_있음
        LineRequest 사당 = 지하철_노선_정보("사당", "blue");
        ExtractableResponse<Response> 사당역 = 지하철_노선_생성_요청(사당);

        // when
        ExtractableResponse<Response> 지하철목록 = 지하철_노선_목록_조회();

        // then
        지하철_노선_응답됨(지하철목록);
        지하철_노선_목록_포함됨(지하철목록, 화곡역, 사당역);
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void getLine() {
        // given
        // 지하철_노선_등록되어_있음
        LineRequest 화곡 = 지하철_노선_정보("화곡", "purple");
        ExtractableResponse<Response> 화곡역 = 지하철_노선_생성_요청(화곡);

        // when
        ExtractableResponse<Response> 응답 = 지하철_노선_조회_요청(화곡역);

        // then
        지하철_노선_응답됨(응답);
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        // 지하철_노선_등록되어_있음
        LineRequest 화곡 = 지하철_노선_정보("화곡", "purple");
        ExtractableResponse<Response> 화곡역 = 지하철_노선_생성_요청(화곡);
        LineRequest 까치산 = 지하철_노선_정보("까치산", "purple");

        // when
        ExtractableResponse<Response> 수정 = 지하철_노선_수정_요청(화곡역, 까치산);

        // then
        지하철_노선_응답됨(수정);
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        // 지하철_노선_등록되어_있음
        LineRequest 화곡 = 지하철_노선_정보("화곡", "purple");
        ExtractableResponse<Response> 화곡역 = 지하철_노선_생성_요청(화곡);

        // when
        ExtractableResponse<Response> 삭제 = 지하철_노선_제거_요청(화곡역);

        // then
        지하철_노선_삭제됨(삭제);
    }

    private LineRequest 지하철_노선_정보(String name, String color) {
        return new LineRequest(name, color);
    }

    private ExtractableResponse<Response> 지하철_노선_생성_요청(LineRequest lineRequest) {
        return RestAssured
            .given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().post(LINES_PATH)
            .then().log().all().extract();
    }

    private ExtractableResponse<Response> 지하철_노선_목록_조회() {
        return RestAssured
            .given().log().all()
            .when().get(LINES_PATH)
            .then().log().all().extract();
    }

    private ExtractableResponse<Response> 지하철_노선_조회_요청(ExtractableResponse<Response> response) {
        return RestAssured
            .given().log().all()
            .body(response.header("Location"))
            .when().get(LINES_PATH)
            .then().log().all().extract();
    }

    private ExtractableResponse<Response> 지하철_노선_수정_요청(ExtractableResponse<Response> response,
        LineRequest changeLine) {
        return RestAssured
            .given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(changeLine)
            .when().put(response.header("Location"))
            .then().log().all().extract();
    }

    private ExtractableResponse<Response> 지하철_노선_제거_요청(ExtractableResponse<Response> response) {
        return RestAssured
            .given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().delete(response.header("Location"))
            .then().log().all().extract();
    }

    private void 지하철_노선_목록_포함됨(ExtractableResponse<Response> resultResponse,
        ExtractableResponse<Response> response1, ExtractableResponse<Response> response2) {

        List<Long> expectedLineIds = Arrays.asList(response1, response2).stream()
            .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
            .collect(Collectors.toList());

        List<Long> resultLineIds = resultResponse.jsonPath().getList(".", LineResponse.class).stream()
            .map(it -> it.getId())
            .collect(Collectors.toList());
        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    private void 지하철_노선_생성됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    private void 지하철_노선_생성_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 지하철_노선_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 지하철_노선_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}
