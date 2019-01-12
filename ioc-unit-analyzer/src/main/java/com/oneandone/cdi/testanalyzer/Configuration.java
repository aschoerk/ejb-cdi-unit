package com.oneandone.cdi.testanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author aschoerk
 */
public class Configuration {

    public TesterExtensionsConfigsFinder testerExtensionsConfigsFinder = null;

    Logger logger = LoggerFactory.getLogger(Configuration.class);

    // used to separate classes which might have configuring annotations and might have priority over
    // each other in case of injections.
    private Set<Class<?>> testClasses = new HashSet<>();

    private Set<Class<?>> enabledAlternatives = new HashSet<>();

    // candidates to be started.
    // The testclass, all classes found in @TestClasses
    // classes usable to fill injects in beansToBeStarted.
    private List<Class<?>> beansToBeStarted = new ArrayList<>(); // these beans must be given to CDI to be started

    // the testclass itself and beans defined by @TestClasses or @SutClasses, or by services
    private List<Class<?>> obligatory = new ArrayList<>();

    // classes defined to be excluded from configuration. Done by servcices or @ExcludedClasses
    private Set<Class<?>> excluded = new HashSet<>();

    public void addCandidate(Class<?> c) {
        if (!candidates.contains(c)) {
            candidates.add(c);
        } else {
            logger.error("candidates already contains {}",c);
        }
    }

    public boolean isCandidate(Class<?> c) {
        return candidates.contains(c);
    }

    public void moveCandidates(Collection<Class<?>> dest) {
        dest.addAll(candidates);
        candidates.clear();
    }

    public boolean emptyCandidates() {
        return candidates.isEmpty();
    }



    // previously available classes to be added to the startconfiguration
    private List<Class<?>> candidates = new ArrayList<>();

    // classes available to be added to configuration
    private Set<Class<?>> available = new HashSet<>();

    // producers available from obligatory and candidates
    private ProducerMap producerMap = new ProducerMap(this, "Obligatory");

    // producers found in availables
    private ProducerMap availableProducerMap = new ProducerMap(this, "Available");

    // producers created by enabled Alternatives
    private ProducerMap alternativesProducerMap = new ProducerMap(this, "Alternatives");

    // injects found in obligatory and candidates
    private Set<QualifiedType> injects = new HashSet<QualifiedType>();

    // injects found in obligatory and candidates which are handled
    private Set<QualifiedType> handledInjects = new HashSet<QualifiedType>();

    private HashMultiMap<Class<?>, QualifiedType> classes2Injects = new HashMultiMap<>();

    private ElseClasses elseClasses = new ElseClasses();

    /**
     * signify clazz as sutClass
     *
     * @param clazz
     * @return
     */
    Configuration sutClass(Class<?> clazz) {
        testClasses.remove(clazz);
        return this;
    }


    /**
     * signify clazz as testclass
     *
     * @param clazz
     * @return
     */
    Configuration testClass(Class<?> clazz) {
        testClasses.add(clazz);
        return this;
    }

    public Configuration testClassCandidates(final Collection<Class<?>> classes) {
        if (classes != null) {
            for (Class<?> c : classes)
                testClass(c)
                        .candidate(c);
        }
        return this;
    }

    /**
     * signify clazz as to be included in starting configuration
     *
     * @param clazz
     * @return
     */
    Configuration obligatory(Class<?> clazz) {
        obligatory.add(clazz);
        available.add(clazz);
        return this;
    }

    /**
     * signify class as available for starting configuration
     *
     * @param clazz
     * @return
     */
    Configuration available(Class<?> clazz) {
        available.add(clazz);
        return this;
    }


    public Configuration candidate(final Class<?> c) {
        addCandidate(c);
        return this;
    }

    public Configuration enabledAlternative(final Class<?> c) {
        if (c.getAnnotation(Alternative.class) == null || c.isAnnotation() && c.getAnnotation(Stereotype.class) == null) {
            logger.error("Invalid enabled Alternative {}", c.getName());
        }
        if (c.isAnnotation() && c.getAnnotation(Stereotype.class) != null)
            elseClasses.foundAlternativeStereotypes.add(c);
        else
            enabledAlternatives.add(c);
        return this;
    }

    public Configuration excluded(final Class<?> c) {
        excluded.add(c);
        return this;
    }

    public boolean isExcluded(final Class<?> c) {
        return excluded.contains(c);
    }


    public Configuration tobeStarted(final Class<?> c) {
        addToBeStarted(c);
        return this;
    }

    public boolean isToBeStarted(Class<?> c) {
        return beansToBeStarted.contains(c);
    }

    public void setTesterExtensionsConfigsFinder(final TesterExtensionsConfigsFinder testerExtensionsConfigsFinder) {
        this.testerExtensionsConfigsFinder = testerExtensionsConfigsFinder;
    }

    public boolean isTestClass(final Class c) {
        return testClasses.contains(c);
    }

    public Configuration inject(final QualifiedType i) {
        logger.trace("Adding Inject {}",i);
        injects.add(i);
        return this;
    }

    Configuration elseClass(Class<?> c) {
        elseClasses.elseClass(c);
        return this;
    }

    public boolean isAvailable(final Class<? extends Annotation> annotationType) {
        return available.contains(annotationType);
    }

    public ProducerMap getProducerMap() {
        return producerMap;
    }

    public ProducerMap getAvailableProducerMap() {
        return availableProducerMap;
    }

    public ProducerMap getAlternativesProducerMap() {
        return alternativesProducerMap;
    }

    public Set<QualifiedType> getInjects() {
        return injects;
    }

    public Set<Class<?>> getExcludedClasses() {
        return excluded;
    }

    public boolean isActiveAlternativeStereoType(final Annotation c) {
        logger.trace("Searching for alternative Stereotype {}", c);
        for (Class stereoType : elseClasses.foundAlternativeStereotypes) {
            if (stereoType.getName().equals(c.annotationType().getName())) {
                logger.trace("Search found alternative Stereotype {}", c);
                return true;
            }
        }
        return false;
    }

    public boolean isEnabledAlternative(final Class declaringClass) {
        return enabledAlternatives.contains(declaringClass);
    }


    public void addToBeStarted(Class<?> c) {
        if (beansToBeStarted.contains(c)) {
            logger.warn("Trying to add {} a second time",c);
        } else {
            beansToBeStarted.add(c);
            obligatory.add(c);
        }
    }

    public boolean isSuTClass(final Class declaringClass) {
        return !testClasses.contains(declaringClass);
    }

    void injectHandled(QualifiedType inject, final QualifiedType producingType) {
        injects.remove(inject);
        handledInjects.add(inject);
        classes2Injects.put(producingType.getDeclaringClass(), inject);
    }

    Set<QualifiedType> getInjectsForClass(Class<?> key) {
        return classes2Injects.get(key);
    }


    public ElseClasses getElseClasses() {
        return elseClasses;
    }

    public Set<Class<?>> getEnabledAlternatives() {
        return enabledAlternatives;
    }

    public List<Class<?>> getObligatory() {
        return obligatory;
    }
}
