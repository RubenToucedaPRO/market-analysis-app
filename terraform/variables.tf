# Repository Configuration
variable "repository_name" {
  description = "Name of the GitHub repository to create"
  type        = string
  default     = "terraform-demo-repo"
  
  validation {
    condition     = can(regex("^[a-zA-Z0-9._-]+$", var.repository_name))
    error_message = "Repository name can only contain alphanumeric characters, periods, dashes, and underscores."
  }
}

variable "description" {
  description = "Description of the GitHub repository"
  type        = string
  default     = "A demo repository created and managed by Terraform"
}

variable "visibility" {
  description = "Visibility of the repository (public or private)"
  type        = string
  default     = "public"
  
  validation {
    condition     = contains(["public", "private"], var.visibility)
    error_message = "Visibility must be either 'public' or 'private'."
  }
}

# Repository Features
variable "has_issues" {
  description = "Enable GitHub Issues for the repository"
  type        = bool
  default     = true
}

variable "has_projects" {
  description = "Enable GitHub Projects for the repository"
  type        = bool
  default     = true
}

variable "has_wiki" {
  description = "Enable GitHub Wiki for the repository"
  type        = bool
  default     = true
}

# Repository Templates
variable "gitignore_template" {
  description = "Gitignore template to use (e.g., 'Python', 'Node', 'Go')"
  type        = string
  default     = "Java"
}

variable "license_template" {
  description = "License template to use (e.g., 'mit', 'apache-2.0', 'gpl-3.0')"
  type        = string
  default     = "mit"
}

# Repository Topics
variable "topics" {
  description = "List of topics/tags for the repository"
  type        = list(string)
  default     = ["terraform", "infrastructure-as-code", "demo", "github", "automation"]
}

# Security and Management
variable "archive_on_destroy" {
  description = "Archive the repository instead of deleting it when destroyed"
  type        = bool
  default     = true
}

variable "enable_branch_protection" {
  description = "Enable branch protection for the main branch"
  type        = bool
  default     = false
}

# File Creation Options
variable "create_github_workflow" {
  description = "Create a sample GitHub Actions workflow"
  type        = bool
  default     = true
}

# Commit Information
variable "commit_author" {
  description = "Name to use for commits made by Terraform"
  type        = string
  default     = "Terraform Bot"
}

variable "commit_email" {
  description = "Email to use for commits made by Terraform"
  type        = string
  default     = "terraform@example.com"
}
