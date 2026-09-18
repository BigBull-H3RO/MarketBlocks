# 📋 Pull Request

### Thank you for contributing to MarketBlocks!

**Before submitting this pull request, please ensure that:**
- You have searched for existing pull requests to prevent duplicates.
- Your branch is based on the latest `main` (or active development branch).
- All changes are atomic and focused on a single topic or fix.

---

## 📝 Description
<!-- Provide a clear and concise description of what this PR does and why it is needed. -->


## 🔗 Related Issues / Discussions
<!-- Link related issues or discussions below (e.g. Fixes #12, Closes #34, Related to #56) -->
- Closes #

---

## 🏷️ Type of Change
<!-- Please check all that apply: -->
- [ ] 🐞 **Bug fix** (non-breaking change which fixes an issue)
- [ ] ✨ **New feature** (non-breaking change which adds functionality)
- [ ] ⚡ **Performance improvement** (optimizations, memory or network reduction)
- [ ] ♻️ **Refactoring** (code organization without changing behavior)
- [ ] 📖 **Documentation** (updates to docs, README, or wiki)
- [ ] 🔧 **Build / CI** (changes to build scripts, Gradle, or workflows)

---

## 📦 Affected Modules
<!-- Which modules does this pull request touch? -->
- [ ] `common` (Shared domain logic, screens, configs, assets)
- [ ] `fabric` (Fabric platform implementation, mixins, renderers)
- [ ] `neoforge` (NeoForge platform implementation, event buses, renderers)
- [ ] `.github` (Workflows, templates, project metadata)

---

## 🧪 Testing & Verification
<!-- How did you test your changes? Please describe steps to verify behavior. -->
- **Environment:**
  - Minecraft Version: `1.21.1`
  - Loader: <!-- NeoForge 21.1.x / Fabric Loader 0.19.x -->
  - Environment: <!-- Singleplayer / Dedicated Server / Both -->

### Steps to test:
1. 
2. 
3. 

<!-- If applicable, add screenshots or videos demonstrating your changes below: -->
### Screenshots / Evidence (optional):


---

## 📋 Pre-Merge Checklist
<!-- Please complete the following checklist before requesting a review: -->
- [ ] My code follows the code style and guidelines of MarketBlocks.
- [ ] I have executed `./gradlew build` locally and it finishes without errors or warnings.
- [ ] I have verified that both Fabric and NeoForge compile successfully.
- [ ] If changing datagen/resources, I have run `./gradlew :neoforge:runData` and committed the generated files.
- [ ] I have tested these changes in-game to ensure no regressions or crashes.
- [ ] No unrelated files, secrets, or temporary debug logs have been included.
