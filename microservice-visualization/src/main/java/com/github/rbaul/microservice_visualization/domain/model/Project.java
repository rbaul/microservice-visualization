package com.github.rbaul.microservice_visualization.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
	private String version;
	
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "project")
	private Set<Application> applications;
	
	@ToString.Exclude
	@ElementCollection
	private Map<String, List<String>> connections;
	
	@ToString.Exclude
	@ElementCollection
	private List<ApplicationGroup> groups;
	
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_version_id")
	private ProjectVersion projectVersion;
	
	public void setApplications(List<Application> applications) {
		if (!CollectionUtils.isEmpty(this.applications)) {
			this.applications.forEach(this::removeApplication);
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
