# Configure the GitHub Provider
terraform {
  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 6.0"
    }
  }
  required_version = ">= 1.0"
}

# Configure the GitHub Provider
provider "github" {
  # Token is read from GITHUB_TOKEN environment variable
  # You can also specify it directly (not recommended for security):
  # token = "your_token_here"
}

# Create a GitHub repository
resource "github_repository" "repo" {
  name        = var.repository_name
  description = var.description
  visibility  = var.visibility

  # Repository features
  has_issues   = var.has_issues
  has_projects = var.has_projects
  has_wiki     = var.has_wiki

  # Git settings
  auto_init          = true
  gitignore_template = var.gitignore_template
  license_template   = var.license_template

  # Topics/tags for the repository
  topics = var.topics

  # Security settings
  vulnerability_alerts = true

  # Archive on destroy (instead of deleting)
  archive_on_destroy = var.archive_on_destroy

  lifecycle {
    prevent_destroy = true
  }
}

# Create a README file in the repository
resource "github_repository_file" "readme" {
  repository = github_repository.repo.name
  branch     = "main"
  file       = "README.md"
  content = templatefile("${path.module}/templates/README.md.tpl", {
    repository_name = var.repository_name
    description     = var.description
    created_by      = "Terraform"
    timestamp       = timestamp()
  })
  commit_message      = "Initial README created by Terraform"
  commit_author       = var.commit_author
  commit_email        = var.commit_email
  overwrite_on_create = true

  depends_on = [github_repository.repo]
}

# Create a basic workflow file (optional)
resource "github_repository_file" "github_workflow" {
  count               = var.create_github_workflow ? 1 : 0
  repository          = github_repository.repo.name
  branch              = "main"
  file                = ".github/workflows/testUnitarios-workflow.yml"
  content             = file("${path.module}/templates/testUnitarios-workflow.yml")
  commit_message      = "Add test unitarios GitHub Actions workflow"
  commit_author       = var.commit_author
  commit_email        = var.commit_email
  overwrite_on_create = true

  depends_on = [github_repository.repo]
}

# Create branch protection rule (optional, only for public repos or with GitHub Pro)
resource "github_branch_protection" "main" {
  count         = var.enable_branch_protection ? 1 : 0
  repository_id = github_repository.repo.node_id
  pattern       = "main"

  required_status_checks {
    strict   = true      # Require branches to be up to date before merging
    contexts = ["build"] # Add your CI status check contexts here
  }

  required_pull_request_reviews {
    required_approving_review_count = 1
    dismiss_stale_reviews           = true
  }

  enforce_admins = true # Enforce for admins as well

  depends_on = [github_repository.repo]
}
