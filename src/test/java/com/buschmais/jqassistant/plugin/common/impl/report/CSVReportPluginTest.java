package com.buschmais.jqassistant.plugin.common.impl.report;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CSVReportPluginTest extends AbstractReportPluginTest {

    private Concept conceptWithRows = Concept.builder().id("test:ConceptWithRows").description("testConceptWithRows").severity(Severity.MINOR).build();

    private Concept conceptWithoutRows = Concept.builder().id("test:ConceptWithoutRows").description("testConceptWithRows").severity(Severity.MINOR).build();

    public CSVReportPluginTest() {
        super(new CSVReportPlugin());
    }

    @Test
    public void conceptWithRows() throws ReportException, IOException {
        Map<String, Object> properties = Collections.emptyMap();
        plugin.configure(reportContext, properties);
        plugin.begin();
        apply(conceptWithRows, SUCCESS);
        plugin.end();

        File csvReportDirectory = reportContext.getReportDirectory("csv");
        assertThat(csvReportDirectory.exists(), equalTo(true));

        File report = new File(csvReportDirectory, "test_conceptWithRows.csv");
        assertThat(report.exists(), equalTo(true));

        String content = FileUtils.readFileToString(report);
        assertThat(content, equalTo("\"String\",\"Double\",\"Named\",\"EscapedString\"\n" + "\"foo\",\"42.0\",\"Test\",\"\"\"'\"\n"));
    }

    @Test
    public void separatorAndEscapeChar() throws ReportException, IOException {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CSVReportPlugin.PROPERTY_SEPARATOR, ";");
        properties.put(CSVReportPlugin.PROPERTY_QUOTE_CHAR, "'");
        properties.put(CSVReportPlugin.PROPERTY_ESCAPE_CHAR, "'");
        plugin.configure(reportContext, properties);
        plugin.begin();
        apply(conceptWithRows, SUCCESS);
        plugin.end();

        File csvReportDirectory = reportContext.getReportDirectory("csv");
        assertThat(csvReportDirectory.exists(), equalTo(true));

        File report = new File(csvReportDirectory, "test_conceptWithRows.csv");
        assertThat(report.exists(), equalTo(true));

        String content = FileUtils.readFileToString(report);
        assertThat(content, equalTo("'String';'Double';'Named';'EscapedString'\n" + "'foo';'42.0';'Test';'\"'''\n"));
    }

    @Test
    public void conceptWithoutRows() throws ReportException, IOException {
        Map<String, Object> properties = Collections.emptyMap();
        plugin.configure(reportContext, properties);
        plugin.begin();
        apply(conceptWithoutRows, SUCCESS);
        plugin.end();

        File csvReportDirectory = reportContext.getReportDirectory("csv");
        assertThat(csvReportDirectory.exists(), equalTo(true));

        File report = new File(csvReportDirectory, "test_conceptWithoutRows.csv");
        assertThat(report.exists(), equalTo(true));

        String content = FileUtils.readFileToString(report);
        assertThat(content, equalTo(""));
    }

    @Override
    protected <T extends ExecutableRule<?>> Result<T> getResult(T rule, Result.Status status) {
        if (rule.equals(conceptWithRows)) {
            HashMap<String, Object> row = new HashMap<>();
            row.put("String", "foo");
            row.put("Double", 42.0);
            row.put("Named", new NamedDescriptor() {
                @Override
                public <I> I getId() {
                    return null;
                }

                @Override
                public <T> T as(Class<T> type) {
                    return null;
                }

                @Override
                public <D> D getDelegate() {
                    return null;
                }

                @Override
                public String getName() {
                    return "Test";
                }

                @Override
                public void setName(String name) {
                }
            });
            row.put("EscapedString", "\"'");
            return Result.<T> builder().rule(rule).severity(rule.getSeverity()).status(status).columnNames(asList("String", "Double", "Named", "EscapedString"))
                    .rows(asList(row)).build();
        } else {
            return Result.<T> builder().rule(rule).severity(rule.getSeverity()).status(status).columnNames(null).rows(null).build();
        }
    }

}
