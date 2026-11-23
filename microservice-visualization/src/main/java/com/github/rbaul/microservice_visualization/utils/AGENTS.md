# utils Package

Utility classes for dependency string parsing and Bitbucket-specific formatting.

## Classes

### `Dependency` (record)
Simple value object: `packageId` (group), `name` (artifact), `version`. Nullable fields for local dependencies (no group/version).

### `ConverterUtils`
Stateless utility (`@UtilityClass`) for dependency string operations:
- `convertDependency(String)` -- parses `group:artifact:version` format into a `Dependency` record. Handles local dependencies (`:artifact:` format, 2 parts) by setting group and version to null.
- `convertDependency(String, String)` -- parses `group:artifact` with a separate version.
- `mapToDependencyManagement(String, Map)` -- converts a dependency management string and its version overrides into a `DependencyManagement` entity.
- `getDependencyFormatted(String...)` -- joins strings with `:` separator.

### `BitbucketConverterUtils`
Stateless utility (`@UtilityClass`) for Bitbucket-specific string formatting:
- `getBranchIdFromName(branchName)` -- converts branch name to ref format (`refs/heads/<name>`)
- `getTagIdFromName(tagName)` -- converts tag name to ref format (`refs/tag/<name>`)
- `getFullName(project, repo)` -- creates `project/repo` identifier (also used by `GithubProjectLoaderService` for GitHub full name)

## Notes
- The dependency format `group:artifact:version` is the standard throughout the application -- all loaders produce it and all services consume it.
- `BitbucketConverterUtils.getFullName()` is reused by the GitHub loader despite the class name, since both platforms use `owner/repo` format.
