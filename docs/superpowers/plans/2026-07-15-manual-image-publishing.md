# 手动镜像发布 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 仅在维护者手动填写正式版本后构建、扫描并发布 GHCR 镜像；普通代码推送不再触发镜像工作流。

**Architecture:** 将 `.github/workflows/image.yml` 的触发器替换为 `workflow_dispatch`，使用一个必填的 `version` 输入作为唯一发布版本来源。校验步骤在任何构建开始前拒绝非 `MAJOR.MINOR.PATCH` 格式；Docker metadata 从该版本派生完整、次版本、latest 与提交 SHA 标签。

**Tech Stack:** GitHub Actions、Bash、docker/metadata-action、Docker Buildx、Trivy、GHCR。

## Global Constraints

- 普通 `push` 和 Pull Request 仍只运行 `CI`，绝不发布镜像。
- 手动输入版本必须是 `MAJOR.MINOR.PATCH`，例如 `0.1.0`；不接受 `v` 前缀或 `SNAPSHOT`。
- 当前 Maven 版本 `0.0.1-SNAPSHOT` 只作为 Actions 手动触发页的文本提示，不作为发布镜像标签。
- 发布必须继续构建一次、扫描同一归档、通过扫描后才推送 GHCR。

---

### Task 1: 将镜像工作流改为手动且可校验的发布流程

**Files:**
- Modify: `.github/workflows/image.yml`

**Interfaces:**
- Consumes: GitHub Actions `workflow_dispatch.inputs.version` 字符串。
- Produces: GHCR 标签 `<version>`、`<major>.<minor>`、`latest`、`sha-<short-sha>`。

- [ ] **Step 1: 写一个会失败的工作流结构检查**

运行：

```bash
node --input-type=module <<'NODE'
import assert from 'node:assert/strict';
import fs from 'node:fs';

const workflow = fs.readFileSync('.github/workflows/image.yml', 'utf8');
assert.match(workflow, /workflow_dispatch:/);
assert.doesNotMatch(workflow, /^\s*push:/m);
assert.match(workflow, /version:\n\s+description:.*0\.0\.1-SNAPSHOT[\s\S]*?required: true[\s\S]*?type: string/);
assert.match(workflow, /\^\[0-9\]\+\\\.\[0-9\]\+\\\.\[0-9\]\+\$/);
assert.match(workflow, /type=semver,pattern=\{\{version\}\},value=\$\{\{ inputs\.version \}\}/);
assert.match(workflow, /type=semver,pattern=\{\{major\}\}\.\{\{minor\}\},value=\$\{\{ inputs\.version \}\}/);
assert.match(workflow, /type=raw,value=latest/);
assert.match(workflow, /image-ref: \$\{\{ env\.REGISTRY \}\}\/\$\{\{ env\.IMAGE_NAME \}\}:\$\{\{ inputs\.version \}\}/);
console.log('manual image workflow contract verified');
NODE
```

- [ ] **Step 2: 运行检查确认当前工作流失败**

运行 Step 1 命令。

预期：失败，因为现有工作流包含 `push` 触发器，且没有 `workflow_dispatch` 版本输入。

- [ ] **Step 3: 实现手动触发、版本校验和标签策略**

将 `.github/workflows/image.yml` 的 `on` 段替换为：

```yaml
on:
  workflow_dispatch:
    inputs:
      version:
        description: "发布版本（当前 Maven 版本：0.0.1-SNAPSHOT；例如 0.1.0）"
        required: true
        type: string
```

在 `docker/metadata-action@v5` 前插入：

```yaml
      - name: Validate release version
        env:
          RELEASE_VERSION: ${{ inputs.version }}
        run: |
          set -euo pipefail
          if [[ ! "$RELEASE_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
            echo "version must be MAJOR.MINOR.PATCH (for example: 0.1.0)" >&2
            exit 1
          fi
```

替换 metadata 的 `tags` 为：

```yaml
          tags: |
            type=semver,pattern={{version}},value=${{ inputs.version }}
            type=semver,pattern={{major}}.{{minor}},value=${{ inputs.version }}
            type=raw,value=latest
            type=sha,prefix=sha-
```

将 Trivy 的 `image-ref` 改为：

```yaml
          image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ inputs.version }}
```

- [ ] **Step 4: 本地验证工作流语法与版本规则**

运行：

```bash
ruby -e 'require "yaml"; YAML.load_file(".github/workflows/image.yml"); puts "YAML valid"'
valid='0.1.0'; [[ "$valid" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]
for invalid in '' 'v0.1.0' '0.1' '0.1.0-SNAPSHOT' '0.1.0.1'; do
  ! [[ "$invalid" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]
done
```

预期：输出 `YAML valid`，并以状态码 0 结束。

- [ ] **Step 5: 重新运行工作流结构检查**

运行 Step 1 命令。

预期：输出 `manual image workflow contract verified`。

- [ ] **Step 6: 提交工作流改动**

```bash
git add .github/workflows/image.yml
git commit -m "ci: publish images manually with release versions"
```

### Task 2: 在 GitHub 上验证触发边界与发布

**Files:**
- Modify: `.github/workflows/image.yml`（仅在 GitHub 执行暴露配置错误时修正）

**Interfaces:**
- Consumes: 已推送的 `image.yml` 与 GitHub Actions `workflow_dispatch`。
- Produces: 成功的 CI；手动发布后 GHCR 中的版本标签。

- [ ] **Step 1: 推送工作流提交**

运行：

```bash
git push origin main
```

预期：只有 `CI` 因普通 push 被触发；不出现新的 `Container image` run。

- [ ] **Step 2: 检查普通推送触发结果**

运行：

```bash
gh run list --repo yumengjh/springboot-start --branch main --limit 5 \
  --json name,headSha,status,conclusion,url
```

预期：当前提交存在 `CI`，但没有该提交的 `Container image`。

- [ ] **Step 3: 手动触发一次发布验证**

在 GitHub Actions 的 `Container image` 工作流中，以 `version=0.1.0` 触发；或在已认证 `gh` 中运行：

```bash
gh workflow run image.yml --repo yumengjh/springboot-start --ref main -f version=0.1.0
```

预期：workflow 先通过版本校验，再构建、扫描、推送同一镜像。

- [ ] **Step 4: 核对最终工作流状态与标签**

运行：

```bash
gh run list --repo yumengjh/springboot-start --workflow 'Container image' --limit 1 \
  --json status,conclusion,url
```

预期：`conclusion` 为 `success`；发布日志显示 `0.1.0`、`0.1`、`latest` 与 `sha-<短提交哈希>` 已推送。

- [ ] **Step 5: 提交任何仅为 GitHub 实测所需的修正**

若无修正，不创建额外提交；若 workflow 语法或 tag 行为不符合本计划，只提交 `.github/workflows/image.yml`，提交信息使用：

```bash
git commit -m "fix(ci): correct manual image release workflow"
```
