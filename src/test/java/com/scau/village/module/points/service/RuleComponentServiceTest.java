package com.scau.village.module.points.service;

import com.scau.village.module.points.entity.RuleComponent;
import com.scau.village.module.points.mapper.RuleComponentMapper;
import com.scau.village.module.points.mapper.RuleComponentResultMapper;
import com.scau.village.module.points.service.impl.RuleComponentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** 门前三包规则子项计分服务单元测试。 */
class RuleComponentServiceTest {

    @Mock
    private RuleComponentMapper componentMapper;

    @Mock
    private RuleComponentResultMapper resultMapper;

    private RuleComponentServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RuleComponentServiceImpl(resultMapper);
        ReflectionTestUtils.setField(service, "baseMapper", componentMapper);
        when(componentMapper.selectList(any())).thenReturn(components());
    }

    @Test
    void allComponentsPassScoresFifteen() {
        assertThat(service.calculateScore(16, Map.of(
                "sanitation", true, "greenery", true, "order", true))).isEqualTo(15);
    }

    @Test
    void sanitationAndGreeneryPassScoreTen() {
        assertThat(service.calculateScore(16, Map.of(
                "sanitation", true, "greenery", true, "order", false))).isEqualTo(10);
    }

    @Test
    void sanitationOnlyPassScoresFive() {
        assertThat(service.calculateScore(16, Map.of(
                "sanitation", true, "greenery", false, "order", false))).isEqualTo(5);
    }

    @Test
    void noComponentPassesScoresZero() {
        assertThat(service.calculateScore(16, Map.of(
                "sanitation", false, "greenery", false, "order", false))).isZero();
    }

    @Test
    void missingComponentIsTreatedAsFalse() {
        Map<String, Boolean> partialResults = new HashMap<>();
        partialResults.put("sanitation", true);

        assertThat(service.calculateScore(16, partialResults)).isEqualTo(5);
    }

    private List<RuleComponent> components() {
        return List.of(component(1L, "sanitation"),
                component(2L, "greenery"), component(3L, "order"));
    }

    private RuleComponent component(Long id, String code) {
        RuleComponent component = new RuleComponent();
        component.setId(id);
        component.setRuleId(16);
        component.setComponentCode(code);
        component.setPoints(5);
        component.setRequired(0);
        return component;
    }
}
