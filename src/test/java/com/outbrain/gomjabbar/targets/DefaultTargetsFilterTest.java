package com.outbrain.gomjabbar.targets;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.outbrain.gomjabbar.testutil.HealthInfoInstanceHealper;
import com.outbrain.ob1k.consul.HealthInfoInstance;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for the {@link DefaultTargetsFilter} implementation
 * @author Eran Harel
 */
public class DefaultTargetsFilterTest {

  private static final String INCLUDED_ELEMENT1 = "inc1";
  private static final String INCLUDED_ELEMENT2 = "inc2";
  private static final String EXCLUDED_ELEMENT1 = "exc1";
  private static final String EXCLUDED_ELEMENT2 = "exc2";
  private static final Set<String> SOME_ELEMENTS = Sets.newHashSet("some1", "some2");
  private static final Set<String> INCLUDED = Sets.newHashSet(INCLUDED_ELEMENT1, INCLUDED_ELEMENT2);
  private static final Set<String> EXCLUDED = Sets.newHashSet(EXCLUDED_ELEMENT1, EXCLUDED_ELEMENT2);

  private final Random random = ThreadLocalRandom.current();

  @Test
  public void testDcFilter() {
    final DefaultTargetsFilter targetsFilter = new DefaultTargetsFilter(INCLUDED, EXCLUDED, randomSet(), randomSet(), randomSet(), randomSet());
    assertIncludeExcludeFilterCorrectness(targetsFilter.dcFilter());
  }

  @Test
  public void testModuleFilter() {
    final DefaultTargetsFilter targetsFilter = new DefaultTargetsFilter(randomSet(), randomSet(), INCLUDED, EXCLUDED, randomSet(), randomSet());
    assertIncludeExcludeFilterCorrectness(targetsFilter.moduleFilter());
  }

  @Test
  public void testEmptyFilters_allShouldPass() {
    final DefaultTargetsFilter targetsFilter = new DefaultTargetsFilter(emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet());
    Arrays.asList(targetsFilter.dcFilter(), targetsFilter.moduleFilter()).forEach(f -> {
      assertElementsFilteringCorrectness(targetsFilter.dcFilter(), INCLUDED, true, "any element should pass");
      assertElementsFilteringCorrectness(targetsFilter.dcFilter(), EXCLUDED, true, "any element should pass");
      assertElementsFilteringCorrectness(targetsFilter.dcFilter(), SOME_ELEMENTS, true, "any element should pass");
    });
  }

  @Test
  public void testInstanceFilter_includeExcludeOnPassingNode() {
    final DefaultTargetsFilter targetsFilter = new DefaultTargetsFilter(randomSet(), randomSet(), randomSet(), randomSet(), INCLUDED, EXCLUDED);
    final Predicate<HealthInfoInstance> filter = targetsFilter.instanceFilter();

    final HealthInfoInstance instance = HealthInfoInstanceHealper.createMockHealthInfoInstance("meh");
    assertElementFilteringCorrectness(filter, instance, false, "Filtering should fail for no matching tags");

    instance.Service.Tags = INCLUDED;
    assertElementFilteringCorrectness(filter, instance, true, "Filtering should pass for no included tags");

    instance.Service.Tags = EXCLUDED;
    assertElementFilteringCorrectness(filter, instance, false, "Filtering should fail for excluded tags");

    instance.Service.Tags = emptySet();
    assertElementFilteringCorrectness(filter, instance, false, "Filtering should fail for no tags");

    instance.Service.Tags = null;
    assertElementFilteringCorrectness(filter, instance, false, "Filtering should fail for null tags");
  }

  @Test
  public void testInstanceFilter_includeExcludeWithNoSpecifiedTags_shouldPassAnyElement() {
    final DefaultTargetsFilter targetsFilter = new DefaultTargetsFilter(randomSet(), randomSet(), randomSet(), randomSet(), emptySet(), emptySet());
    final Predicate<HealthInfoInstance> filter = targetsFilter.instanceFilter();

    final HealthInfoInstance instance = HealthInfoInstanceHealper.createMockHealthInfoInstance("meh");
    assertElementFilteringCorrectness(filter, instance, true, "Filtering should pass for any tags");

    instance.Service.Tags = randomSet();
    assertElementFilteringCorrectness(filter, instance, true, "Filtering should pass for any tags");

    instance.Service.Tags = emptySet();
    assertElementFilteringCorrectness(filter, instance, true, "Filtering should pass for any tags");

    instance.Service.Tags = null;
    assertElementFilteringCorrectness(filter, instance, true, "Filtering should pass for any tags");
  }

  @Test
  public void testInstanceFilter_includeExcludeOnFailingNode_shouldFailAnyElement() {
    final DefaultTargetsFilter targetsFilter = new DefaultTargetsFilter(randomSet(), randomSet(), randomSet(), randomSet(), INCLUDED, EXCLUDED);
    final Predicate<HealthInfoInstance> filter = targetsFilter.instanceFilter();

    final HealthInfoInstance instance = HealthInfoInstanceHealper.createMockHealthInfoInstance("meh");
    final HealthInfoInstance.Check check = new HealthInfoInstance.Check();
    check.Status = "Not passing!!!!11";
    instance.Checks = Lists.newArrayList(check);

    assertElementFilteringCorrectness(filter, instance, false, "Filtering should fail for failing nodes");

    instance.Service.Tags = randomSet();
    assertElementFilteringCorrectness(filter, instance, false, "Filtering should pass for failing nodes");
  }

  private void assertIncludeExcludeFilterCorrectness(final Predicate<String> filter) {
    assertElementsFilteringCorrectness(filter, INCLUDED, true, "Included element should pass the filter");
    assertElementsFilteringCorrectness(filter, EXCLUDED, false, "excluded element shouldn't pass the filter");
    assertElementsFilteringCorrectness(filter, SOME_ELEMENTS, false, "Irrelevant element shouldn't pass the filter");
  }

  private <T> void assertElementsFilteringCorrectness(final Predicate<T> filter, final Set<T> testElements, boolean shouldPassFilter, final String msg) {
    testElements.forEach(e -> assertElementFilteringCorrectness(filter, e, shouldPassFilter, msg));
  }

  private <T> void assertElementFilteringCorrectness(final Predicate<T> filter, final T testElement, boolean shouldPassFilter, final String msg) {
    assertTrue(msg, filter.test(testElement) == shouldPassFilter);
  }

  private Set<String> randomSet() {
    return random.ints(random.nextInt(3))
      .boxed().map(Object::toString)
      .collect(Collectors.toSet());
  }

}