package com.github.rbaul.microservice_visualization.domain.model;

import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
public class ProjectVersion {
	@Id
	@GeneratedValue
	private Short id;

	@NotEmpty
	@Column(unique = true)
	private String name;
	
	private String description;

	@NotEmpty
	@Column(unique = true)
	private String fullName;

	@Embedded
	private LoaderDetails loaderDetails;
	
	@ToString.Exclude
	@OneToOne(fetch = FetchType.LAZY)
	private Project mainProject;
	
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "projectVersion")
	private Set<Project> projects;
	
	public void setProjects(List<Project> projects) {
		if (!CollectionUtils.isEmpty(this.projects)) {
			this.projects.forEach(this::removeProject);
		}
		
		if (!CollectionUtils.isEmpty(projects)) {
			projects.forEach(this::addProject);
		}
	}
	
	public void addProject(Project project) {
		if (projects == null) {
			projects = new HashSet<>();
		}
		project.setProjectVersion(this);
		projects.add(project);
	}
	
	public void removeProject(Project project) {
		if (projects != null) {
			projects.remove(project);
			project.setProjectVersion(null);
		}
	}
}
