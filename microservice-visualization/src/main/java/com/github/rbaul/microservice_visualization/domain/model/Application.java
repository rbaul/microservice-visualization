package com.github.rbaul.microservice_visualization.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
public class Application {
	@Id
	@GeneratedValue
	private Long id;
	
	@NotEmpty
	private String name;
	
	private String label;
	
	private String description;

	@Column(name = "artifact_group")
	private String group;

	private String version;

	private String location;

	@ElementCollection
	private Set<@NotEmpty String> owners;

	@Enumerated(EnumType.STRING)
	private ApplicationType type;
	
	@ElementCollection(fetch = FetchType.EAGER)
	private Map<String, String> tags;
	
	@ElementCollection
	private List<@NotEmpty String> dependencies;

	@ElementCollection
	private List<@NotNull DependencyEntity> fullDependencies;

	@ToString.Exclude
	@ElementCollection
	private List<@NotNull DependencyManagement> dependencyManagements;
	
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;
	
}
