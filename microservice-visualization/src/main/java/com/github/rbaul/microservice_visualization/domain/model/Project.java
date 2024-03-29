package com.github.rbaul.microservice_visualization.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
public class Project {
	@Id
	@GeneratedValue
	private Short id;

	@NotEmpty
	private String versionId;

	@NotEmpty
	private String version;
	
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "project")
	private Set<Application> applications;
	
	@ToString.Exclude
	@ElementCollection
	private Map<String, List<String>> connections;

	@ToString.Exclude
	@ElementCollection
	private Map<String, List<String>> dependencies;

	@ToString.Exclude
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	@Basic(fetch = FetchType.LAZY)
	private Set<List<String>> librariesCycle;
	
	@ToString.Exclude
	@ElementCollection
	private List<ApplicationGroup> groups;
	
	@ToString.Exclude
	@ElementCollection
	private List<Owner> owners;

	@ToString.Exclude
	@ElementCollection
	private Set<String> tags;
	
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_version_id")
	private ProjectVersion projectVersion;
	
	public void setApplications(List<Application> applications) {
		if (!CollectionUtils.isEmpty(this.applications)) {
			this.applications.forEach(application -> application.setProject(null));
			this.applications.clear();
		}

		if (!CollectionUtils.isEmpty(applications)) {
			applications.forEach(this::addApplication);
		}
	}
	
	public void addApplication(Application application) {
		if (applications == null) {
			applications = new HashSet<>();
		}
		application.setProject(this);
		applications.add(application);
	}
	
	public void removeApplication(Application application) {
		if (applications != null) {
			applications.remove(application);
			application.setProject(null);
		}
	}
}
