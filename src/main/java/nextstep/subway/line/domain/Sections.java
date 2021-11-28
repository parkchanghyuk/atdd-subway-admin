package nextstep.subway.line.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

import nextstep.subway.exception.DefaultException;
import nextstep.subway.exception.SectionSortingException;
import nextstep.subway.station.domain.Station;

@Embeddable
public class Sections {
    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL)
    private List<Section> sections = Collections.unmodifiableList(new ArrayList<>());

    protected Sections() {
    }

    private Sections(List<Section> sections) {
        this.sections = Collections.unmodifiableList(sections);
    }

    public Sections merge(Section addSection) {
        List<Section> afterSections = new ArrayList<>(this.sections);
        afterSections.add(addSection);
        return new Sections(afterSections);
    }

    public List<Station> getSortedStations() {
        if (isStationsEmpty()) {
            return new ArrayList<>();
        }
        Section firstSection = findFirstSection();
        return findAllStations(firstSection);
    }

    private boolean isStationsEmpty() {
        return sections.size() <= 0;
    }

    private List<Station> findAllStations(Section firstSection) {
        List<Station> upStations = sections.stream().map(Section::getUpStation).collect(Collectors.toList());
        List<Station> stations = Arrays.asList(firstSection.getUpStation(), firstSection.getDownStation());
        Section lastSection = firstSection;
        while (upStations.contains(lastSection.getDownStation())) {
            Station finalDownStation = lastSection.getDownStation();
            lastSection = sections.stream()
                .filter(section -> section.getUpStation() == finalDownStation)
                .findFirst()
                .orElseThrow(SectionSortingException::new);
            stations.add(lastSection.getDownStation());
        }
        return stations;
    }

    private Section findFirstSection() {
        List<Station> downStations = sections.stream().map(Section::getDownStation).collect(Collectors.toList());
        Section firstSection = sections.get(0);
        while (downStations.contains(firstSection.getUpStation())) {
            Station finalUpStation = firstSection.getUpStation();
            firstSection = sections.stream()
                .filter(section -> section.getDownStation() == finalUpStation)
                .findFirst()
                .orElseThrow(SectionSortingException::new);
        }
        return firstSection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Sections sections1 = (Sections)o;
        return Objects.equals(sections, sections1.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sections);
    }
}
