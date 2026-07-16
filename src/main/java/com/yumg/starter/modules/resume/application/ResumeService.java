package com.yumg.starter.modules.resume.application;

import tools.jackson.databind.ObjectMapper;
import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.ResumeDocument;
import com.yumg.starter.modules.resume.infrastructure.ResumeDocumentRepository;
import com.yumg.starter.modules.security.application.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumeService {

    private static final int CURRENT_SCHEMA_VERSION = 1;
    private final ResumeDocumentRepository documents;
    private final AuditService audit;
    private final ResumeDocumentValidator validator;

    public ResumeService(ResumeDocumentRepository documents, AuditService audit, ObjectMapper objectMapper) {
        this.documents = documents;
        this.audit = audit;
        this.validator = new ResumeDocumentValidator(objectMapper);
    }

    @Transactional(readOnly = true)
    public ResumeDocumentContent publicDocument() {
        return toContent(requireDocument());
    }

    @Transactional(readOnly = true)
    public ResumeDocumentContent managedDocument() {
        return toContent(requireDocument());
    }

    @Transactional
    public ResumeDocumentContent createDefaultIfAbsent() {
        return documents.findFirstByOrderByCreatedAtAsc().map(this::toContent)
                .orElseGet(() -> toContent(documents.save(new ResumeDocument(defaultContent(), CURRENT_SCHEMA_VERSION))));
    }

    @Transactional
    public ResumeDocumentContent update(String content, int schemaVersion, long version) {
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw ApiException.invalidParameter();
        }
        ResumeDocument document = requireDocument();
        if (document.getVersion() != version) {
            throw ApiException.conflict();
        }
        validator.validate(content);
        document.replace(content, schemaVersion);
        documents.flush();
        audit.event("RESUME_UPDATED", "ResumeDocument", document.getId());
        return toContent(document);
    }

    private ResumeDocument requireDocument() {
        return documents.findFirstByOrderByCreatedAtAsc().orElseThrow(ApiException::notFound);
    }

    private ResumeDocumentContent toContent(ResumeDocument document) {
        return new ResumeDocumentContent(document.getContent(), document.getSchemaVersion(), document.getVersion());
    }

    private String defaultContent() {
        return """
                {
                  "seo": {"title": "前端开发实习简历", "description": "前端开发实习简历"},
                  "profile": {
                    "name": "简历所有者",
                    "headline": "前端开发实习",
                    "contacts": [
                      {"icon": "ri:smartphone-line", "label": "电话", "text": "请在后台填写", "href": ""},
                      {"icon": "ri:mail-line", "label": "邮箱", "text": "hi@yumg.top", "href": "mailto:hi@yumg.top"},
                      {"icon": "ri:github-line", "label": "GitHub", "text": "yumengjh", "href": "https://github.com/yumengjh"},
                      {"icon": "ri:men-line", "label": "性别", "text": "男", "href": ""},
                      {"icon": "ri:school-line", "label": "学校", "text": "大学（计算机科学与技术）", "href": ""},
                      {"icon": "ri:graduation-cap-line", "label": "学历", "text": "本科（2026 届）", "href": ""}
                    ]
                  },
                  "sections": [
                    {"id": "summary", "type": "bullet-list", "title": "个人简介", "items": ["热衷于研究前端框架底层原理，具备独立实现模板引擎、路由器等核心模块的能力。", "自驱力强，独立完成多个工程化项目，习惯阅读源码并复现关键机制。", "正在积极探索 Vue、Nuxt、NestJS、Supabase 等前后端技术栈。"]},
                    {"id": "skills", "type": "bullet-list", "title": "个人技能", "items": ["前端基础：熟练掌握 HTML5、CSS3、ES6+，理解原型链、事件循环、模块化、浏览器渲染机制。", "前端框架：熟练使用 Vue 3、Nuxt、Pinia，掌握组件化、SSR、路由、状态管理。", "工程化：熟练 Vite、TypeScript、ESBuild，了解 Rollup、Bun，掌握前端构建优化与打包分析。", "全栈探索：了解 Node.js、NestJS、Supabase（认证、数据库、存储、实时）。", "源码理解：阅读过 Petite-vue、Vue Router、Navigo 源码，具备手写框架核心模块能力。"]},
                    {"id": "experience", "type": "timeline", "title": "技术经历", "items": [{"name": "开源项目探索", "description": "前端框架与底层原理研究", "from": "2024.10", "to": "现在", "bullets": ["深入阅读 Petite-vue 源码并进行代码调试与结构优化，提出潜在改进点。", "研究 Vue Router 与 Navigo 的路由匹配策略、History 管理实现机制。", "基于 NestJS + Supabase 实践全栈开发，探索认证、数据库、存储、实时功能。", "持续构建个人技术体系，对前端工程化、JS 编译器、模板引擎等方向进行深入研究。"]}]},
                    {"id": "projects", "type": "projects", "title": "项目经验", "items": [{"name": "Mist.js", "description": "轻量级类 Vue 前端框架", "role": "核心开发者", "link": "https://github.com/yumengjh/mist", "from": "2024.10", "to": "2025.02", "techs": [], "bullets": ["从零实现类 Vue 的模板编译器，支持插值表达式、指令系统、m-for 等核心能力。", "自研虚拟 DOM diff、依赖收集与 DOM 更新机制，显著提升渲染性能。", "支持对象、数组、数字遍历、块复用、key diff，设计思路参考 Vue 核心机制。", "整体架构轻量、模块划分明确，适合快速构建中小型应用。"]}, {"name": "js-router", "description": "类 Vue Router 的前端路由器", "role": "独立开发者", "link": "https://github.com/yumengjh/js-router", "from": "2024.08", "to": "2024.12", "techs": [], "bullets": ["手写支持 Hash、History 模式的 SPA 路由器，实现路由匹配与参数解析。", "提供 push、replace、go 等导航 API，兼容静态与动态路由。", "实现 router-view、router-link 自定义元素，封装导航逻辑与事件处理。", "支持页面加载进度条、基本导航守卫，增强用户体验。"]}, {"name": "个人主页（blog.yumg.top）", "description": "技术内容展示", "role": "笔者", "link": "https://blog.yumg.top", "from": "2024.06", "to": "2025.01", "techs": ["Nuxt", "Vue 3", "TypeScript"], "bullets": ["基于 Vue 构建的个人技术博客，支持 SSR、暗黑模式、路由切换动效。", "实现文章系统、归档、标签页等核心功能并支持自动化组件注册。", "使用 Vite 优化构建速度并进行资源按需加载。", "在博客中记录前端、源码分析等内容，累计 150+ 技术文档撰写。"]}]},
                    {"id": "certificates", "type": "bullet-list", "title": "个人证书", "items": ["计算机等级考试", "英语等级考试", "GitHub 连续学习记录与开源项目产出"]}
                  ]
                }
                """;
    }
}
