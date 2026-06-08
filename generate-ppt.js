const pptxgen = require("pptxgenjs");
const React = require("react");
const ReactDOMServer = require("react-dom/server");
const sharp = require("sharp");

// Import icons
const { FaRobot, FaBrain, FaShieldAlt, FaCogs, FaLayerGroup, FaExchangeAlt,
        FaDatabase, FaLock, FaChartLine, FaRocket, FaCheckCircle, FaTimesCircle,
        FaArrowRight, FaBolt, FaServer, FaEye, FaUserShield, FaSitemap,
        FaProjectDiagram, FaSyncAlt, FaBuilding, FaClipboardList } = require("react-icons/fa");

// Color palette (refined from original, no # prefix for pptxgenjs)
const C = {
  bgDark:    "0F172A",  // Dark navy background
  bgDarkAlt: "1E293B",  // Slightly lighter navy
  primary:   "065A82",  // Deep blue
  teal:      "22A699",  // Teal accent
  textDark:  "1E293B",  // Dark text
  textMed:   "64748B",  // Medium gray text
  textLight: "94A3B8",  // Light gray text
  orange:    "E78C07",  // Orange accent
  red:       "BB0000",  // Red/danger
  green:     "107E3E",  // Green
  lightFill: "F1F5F9",  // Very light gray fill
  cardBg:    "FFFFFF",  // White card bg
  white:     "FFFFFF",
  lightTeal: "E6F7F5",  // Light teal bg
  lightBlue: "E8F4F8",  // Light blue bg
  lightGreen:"EDF7ED",  // Light green bg
  lightOrange:"FEF3E6", // Light orange bg
  slate200:  "E2E8F0",
  slate100:  "F8FAFC",
};

// Icon rendering helper
function renderIconSvg(IconComponent, color, size) {
  return ReactDOMServer.renderToStaticMarkup(
    React.createElement(IconComponent, { color, size: String(size) })
  );
}

async function iconToBase64Png(IconComponent, color, size) {
  const svg = renderIconSvg(IconComponent, color, size || 256);
  const pngBuffer = await sharp(Buffer.from(svg)).png().toBuffer();
  return "image/png;base64," + pngBuffer.toString("base64");
}

// Shadow factory (fresh object each time)
const cardShadow = () => ({ type: "outer", color: "000000", blur: 6, offset: 2, angle: 135, opacity: 0.1 });

async function main() {
  let pres = new pptxgen();
  pres.layout = "LAYOUT_16x9";
  pres.author = "本体建模平台";
  pres.title = "本体建模平台 - 产品介绍";

  // Pre-render all icons
  const icons = {};
  const iconList = [
    ["robot", FaRobot, C.primary],
    ["brain", FaBrain, C.teal],
    ["shield", FaShieldAlt, C.teal],
    ["cogs", FaCogs, C.primary],
    ["layers", FaLayerGroup, C.teal],
    ["exchange", FaExchangeAlt, C.orange],
    ["database", FaDatabase, C.primary],
    ["lock", FaLock, C.red],
    ["chart", FaChartLine, C.green],
    ["rocket", FaRocket, C.teal],
    ["check", FaCheckCircle, C.green],
    ["times", FaTimesCircle, C.red],
    ["arrow", FaArrowRight, C.teal],
    ["bolt", FaBolt, C.orange],
    ["server", FaServer, C.primary],
    ["eye", FaEye, C.teal],
    ["userShield", FaUserShield, C.teal],
    ["sitemap", FaSitemap, C.primary],
    ["project", FaProjectDiagram, C.teal],
    ["sync", FaSyncAlt, C.primary],
    ["building", FaBuilding, C.primary],
    ["clipboard", FaClipboardList, C.orange],
    // White versions for dark bg
    ["robotW", FaRobot, C.white],
    ["brainW", FaBrain, "#94A3B8"],
    ["shieldW", FaShieldAlt, C.white],
    ["layersW", FaLayerGroup, C.white],
    ["exchangeW", FaExchangeAlt, C.white],
    ["cogsW", FaCogs, C.white],
    ["boltW", FaBolt, C.orange],
    ["arrowW", FaArrowRight, C.white],
    ["projectW", FaProjectDiagram, C.white],
    ["buildingW", FaBuilding, C.white],
    ["chartW", FaChartLine, C.white],
    ["syncW", FaSyncAlt, C.white],
    ["rocketW", FaRocket, C.white],
    // Colored circle icons
    ["robotTeal", FaRobot, C.teal],
    ["brainBlue", FaBrain, C.primary],
    ["lockRed", FaLock, C.red],
    ["boltOrange", FaBolt, C.orange],
  ];

  for (const [name, Icon, color] of iconList) {
    icons[name] = await iconToBase64Png(Icon, color, 256);
  }

  // ========== SLIDE 1: Cover ==========
  let s1 = pres.addSlide();
  s1.background = { color: C.bgDark };

  // Top teal accent bar
  s1.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Main title
  s1.addText("本体建模平台", {
    x: 0.8, y: 1.4, w: 8.4, h: 1.0,
    fontSize: 44, fontFace: "Arial", color: C.white, bold: true, margin: 0
  });

  // Subtitle
  s1.addText("企业 Agentic AI 时代的业务语义中台", {
    x: 0.8, y: 2.5, w: 8.4, h: 0.6,
    fontSize: 22, fontFace: "Arial", color: C.teal, margin: 0
  });

  // Tagline
  s1.addText("让 AI Agent 100% 理解并安全执行业务操作", {
    x: 0.8, y: 3.3, w: 8.4, h: 0.5,
    fontSize: 14, fontFace: "Arial", color: C.textLight, margin: 0
  });

  // Bottom icons row
  const bottomIcons = ["robotW", "layersW", "shieldW", "cogsW", "exchangeW"];
  const iconLabels = ["Agent 意图", "本体模型", "安全合规", "MCP 分发", "系统集成"];
  for (let i = 0; i < 5; i++) {
    const xPos = 0.8 + i * 1.7;
    s1.addImage({ data: icons[bottomIcons[i]], x: xPos, y: 4.2, w: 0.4, h: 0.4 });
    s1.addText(iconLabels[i], {
      x: xPos + 0.5, y: 4.22, w: 1.2, h: 0.36,
      fontSize: 11, fontFace: "Arial", color: C.textLight, margin: 0, valign: "middle"
    });
  }

  // Bottom line
  s1.addShape(pres.shapes.RECTANGLE, { x: 0, y: 5.2, w: 10, h: 0.02, fill: { color: C.teal } });

  // ========== SLIDE 2: Table of Contents ==========
  let s2 = pres.addSlide();
  s2.background = { color: C.white };

  // Top accent bar
  s2.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Title
  s2.addText("目录", {
    x: 0.6, y: 0.3, w: 4, h: 0.6,
    fontSize: 32, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // 6 chapters in 2 columns
  const chapters = [
    { num: "01", title: "企业 Agentic AI 时代", desc: "为什么需要本体建模", icon: "robot" },
    { num: "02", title: "本体模型的角色定位", desc: "Agent 与系统间的语义桥梁", icon: "sitemap" },
    { num: "03", title: "Agent 如何调用本体", desc: "MCP 分发 · 三通道合规", icon: "exchange" },
    { num: "04", title: "本体模型的构成", desc: "语义 · 动力 · 事件 · 治理", icon: "layers" },
    { num: "05", title: "本体模型的迭代适配", desc: "全生命周期演进策略", icon: "sync" },
    { num: "06", title: "企业运维体系", desc: "监控 · 多租户 · 国产化", icon: "building" },
  ];

  for (let i = 0; i < 6; i++) {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const xPos = 0.6 + col * 4.6;
    const yPos = 1.2 + row * 1.35;
    const ch = chapters[i];

    // Card background
    s2.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: yPos, w: 4.2, h: 1.15,
      fill: { color: C.lightFill }, rectRadius: 0.08
    });

    // Left accent bar
    s2.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: yPos, w: 0.06, h: 1.15,
      fill: { color: C.teal }
    });

    // Chapter number
    s2.addText(ch.num, {
      x: xPos + 0.2, y: yPos + 0.1, w: 0.8, h: 0.5,
      fontSize: 22, fontFace: "Arial", color: C.teal, bold: true, margin: 0
    });

    // Icon
    s2.addImage({ data: icons[ch.icon], x: xPos + 0.9, y: yPos + 0.15, w: 0.35, h: 0.35 });

    // Title
    s2.addText(ch.title, {
      x: xPos + 1.35, y: yPos + 0.1, w: 2.7, h: 0.4,
      fontSize: 15, fontFace: "Arial", color: C.textDark, bold: true, margin: 0, valign: "middle"
    });

    // Description
    s2.addText(ch.desc, {
      x: xPos + 1.35, y: yPos + 0.55, w: 2.7, h: 0.4,
      fontSize: 12, fontFace: "Arial", color: C.textMed, margin: 0, valign: "top"
    });
  }

  // ========== SLIDE 3: 01 - Enterprise Agentic AI Era ==========
  let s3 = pres.addSlide();
  s3.background = { color: C.bgDark };

  // Top teal accent bar
  s3.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s3.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s3.addText("01", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 20, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });
  s3.addText("企业 Agentic AI 时代", {
    x: 1.7, y: 0.3, w: 5, h: 0.5,
    fontSize: 20, fontFace: "Arial", color: C.white, bold: true, margin: 0, valign: "middle"
  });

  // Three big stats
  const stats = [
    { value: "85%", label: "企业将在 2026 年\n部署 AI Agent", icon: "robotW", color: C.teal },
    { value: "60%", label: "Agent 调用因语义\n理解错误而失败", icon: "boltW", color: C.orange },
    { value: "3x", label: "有本体约束的 Agent\n执行成功率提升 3 倍", icon: "chartW", color: C.green },
  ];

  for (let i = 0; i < 3; i++) {
    const xPos = 0.6 + i * 3.1;
    const st = stats[i];

    // Stat card
    s3.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 1.3, w: 2.8, h: 3.5,
      fill: { color: C.bgDarkAlt }, rectRadius: 0.1
    });

    // Top color bar
    s3.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 1.3, w: 2.8, h: 0.06, fill: { color: st.color }
    });

    // Icon
    s3.addImage({ data: icons[st.icon], x: xPos + 1.05, y: 1.6, w: 0.6, h: 0.6 });

    // Big number
    s3.addText(st.value, {
      x: xPos, y: 2.4, w: 2.8, h: 0.8,
      fontSize: 40, fontFace: "Arial", color: st.color, bold: true, align: "center", margin: 0
    });

    // Description
    s3.addText(st.label, {
      x: xPos + 0.3, y: 3.3, w: 2.2, h: 1.0,
      fontSize: 13, fontFace: "Arial", color: C.textLight, align: "center", margin: 0
    });
  }

  // ========== SLIDE 4: Four Challenges ==========
  let s4 = pres.addSlide();
  s4.background = { color: C.white };

  // Top accent bar
  s4.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s4.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s4.addText("01", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 18, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  // Title
  s4.addText("企业 AI 落地的四大根本挑战", {
    x: 0.6, y: 0.9, w: 8.8, h: 0.5,
    fontSize: 26, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  const challenges = [
    { title: "语义鸿沟", desc: "大模型不理解 ERP 里的\n「客户」和 CRM 里的\n「客户」是同一概念", icon: "brainBlue", color: C.primary, bgColor: C.lightBlue },
    { title: "幻觉失控", desc: "Agent 自由调用 API，\n缺少结构化约束，\n产生不可预测行为", icon: "lockRed", color: C.red, bgColor: "FEF2F2" },
    { title: "执行闭环缺失", desc: "Agent 只能「读」不能\n「写」，无法完成完整\n业务流程", icon: "boltOrange", color: C.orange, bgColor: C.lightOrange },
    { title: "知识流失", desc: "业务规则散落在文档\n和代码中，无统一\n结构化表达", icon: "database", color: C.primary, bgColor: C.lightBlue },
  ];

  for (let i = 0; i < 4; i++) {
    const xPos = 0.6 + i * 2.3;
    const ch = challenges[i];

    // Card
    s4.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 1.7, w: 2.1, h: 3.2,
      fill: { color: ch.bgColor }, rectRadius: 0.08
    });

    // Icon circle
    s4.addShape(pres.shapes.OVAL, {
      x: xPos + 0.65, y: 1.95, w: 0.7, h: 0.7,
      fill: { color: ch.color }
    });
    s4.addImage({ data: icons[ch.icon], x: xPos + 0.79, y: 2.1, w: 0.42, h: 0.42 });

    // Title
    s4.addText(ch.title, {
      x: xPos, y: 2.85, w: 2.1, h: 0.4,
      fontSize: 16, fontFace: "Arial", color: ch.color, bold: true, align: "center", margin: 0
    });

    // Description
    s4.addText(ch.desc, {
      x: xPos + 0.15, y: 3.35, w: 1.8, h: 1.2,
      fontSize: 12, fontFace: "Arial", color: C.textMed, align: "center", margin: 0
    });
  }

  // ========== SLIDE 5: Core Value ==========
  let s5 = pres.addSlide();
  s5.background = { color: C.white };

  // Top accent bar
  s5.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s5.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s5.addText("01", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 18, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  // Title
  s5.addText("核心价值：填补语义鸿沟", {
    x: 0.6, y: 0.9, w: 8.8, h: 0.5,
    fontSize: 24, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // Left: Large Model box
  s5.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 1.7, w: 2.5, h: 2.5,
    fill: { color: C.lightBlue }, rectRadius: 0.08
  });
  s5.addText("大模型", {
    x: 0.6, y: 1.85, w: 2.5, h: 0.4,
    fontSize: 16, fontFace: "Arial", color: C.primary, bold: true, align: "center", margin: 0
  });
  s5.addImage({ data: icons.brain, x: 1.3, y: 2.4, w: 0.8, h: 0.8 });
  s5.addText("理解自然语言\n但不懂业务规则", {
    x: 0.6, y: 3.3, w: 2.5, h: 0.6,
    fontSize: 12, fontFace: "Arial", color: C.textMed, align: "center", margin: 0
  });

  // Center: Ontology Platform (bridge)
  s5.addShape(pres.shapes.RECTANGLE, {
    x: 3.6, y: 1.7, w: 2.8, h: 2.5,
    fill: { color: C.lightTeal }, rectRadius: 0.08
  });
  s5.addShape(pres.shapes.RECTANGLE, {
    x: 3.6, y: 1.7, w: 2.8, h: 0.06, fill: { color: C.teal }
  });
  s5.addText("本体建模平台", {
    x: 3.6, y: 1.85, w: 2.8, h: 0.4,
    fontSize: 16, fontFace: "Arial", color: C.teal, bold: true, align: "center", margin: 0
  });
  s5.addImage({ data: icons.layers, x: 4.5, y: 2.4, w: 0.8, h: 0.8 });
  s5.addText("业务语义中台\n翻译 · 约束 · 护航", {
    x: 3.6, y: 3.3, w: 2.8, h: 0.6,
    fontSize: 12, fontFace: "Arial", color: C.textMed, align: "center", margin: 0
  });

  // Right: Business Systems box
  s5.addShape(pres.shapes.RECTANGLE, {
    x: 6.9, y: 1.7, w: 2.5, h: 2.5,
    fill: { color: C.lightOrange }, rectRadius: 0.08
  });
  s5.addText("业务系统", {
    x: 6.9, y: 1.85, w: 2.5, h: 0.4,
    fontSize: 16, fontFace: "Arial", color: C.orange, bold: true, align: "center", margin: 0
  });
  s5.addImage({ data: icons.server, x: 7.6, y: 2.4, w: 0.8, h: 0.8 });
  s5.addText("ERP / CRM / SCM\n精确但语义封闭", {
    x: 6.9, y: 3.3, w: 2.5, h: 0.6,
    fontSize: 12, fontFace: "Arial", color: C.textMed, align: "center", margin: 0
  });

  // Arrows between boxes
  s5.addShape(pres.shapes.LINE, {
    x: 3.1, y: 2.9, w: 0.5, h: 0,
    line: { color: C.teal, width: 2 }
  });
  s5.addShape(pres.shapes.LINE, {
    x: 6.4, y: 2.9, w: 0.5, h: 0,
    line: { color: C.teal, width: 2 }
  });

  // Bottom tagline
  s5.addText("本体 = 大模型与业务系统之间的「翻译器 + 安全员」", {
    x: 0.6, y: 4.5, w: 8.8, h: 0.5,
    fontSize: 14, fontFace: "Arial", color: C.teal, bold: true, align: "center", margin: 0
  });

  // ========== SLIDE 6: What is Ontology ==========
  let s6 = pres.addSlide();
  s6.background = { color: C.white };

  // Top accent bar
  s6.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s6.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s6.addText("01", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 18, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  // Title
  s6.addText("什么是本体（Ontology）", {
    x: 0.6, y: 0.9, w: 8.8, h: 0.5,
    fontSize: 24, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // Two columns
  // Left: What is ontology
  s6.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 1.6, w: 4.2, h: 1.3,
    fill: { color: C.cardBg }, shadow: cardShadow()
  });
  s6.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 1.6, w: 0.06, h: 1.3, fill: { color: C.primary }
  });
  s6.addText([
    { text: "本体是什么", options: { bold: true, color: C.primary, fontSize: 14, breakLine: true } },
    { text: "源自哲学：研究「存在」的学问", options: { color: C.textMed, fontSize: 12, breakLine: true } },
    { text: "在计算机科学中：对特定领域概念、属性、关系的形式化描述", options: { color: C.textMed, fontSize: 12 } },
  ], { x: 0.8, y: 1.7, w: 3.9, h: 1.1, margin: 0, valign: "top", paraSpaceAfter: 4 });

  // Right: Why need ontology
  s6.addShape(pres.shapes.RECTANGLE, {
    x: 5.2, y: 1.6, w: 4.2, h: 1.3,
    fill: { color: C.cardBg }, shadow: cardShadow()
  });
  s6.addShape(pres.shapes.RECTANGLE, {
    x: 5.2, y: 1.6, w: 0.06, h: 1.3, fill: { color: C.orange }
  });
  s6.addText([
    { text: "为什么需要本体", options: { bold: true, color: C.orange, fontSize: 14, breakLine: true } },
    { text: "让机器「理解」业务而非仅「匹配」关键词", options: { color: C.textMed, fontSize: 12, breakLine: true } },
    { text: "统一不同系统间的业务语义", options: { color: C.textMed, fontSize: 12 } },
  ], { x: 5.4, y: 1.7, w: 3.9, h: 1.1, margin: 0, valign: "top", paraSpaceAfter: 4 });

  // Three layers
  const layers = [
    { num: "1", title: "概念层", desc: "定义「客户」「订单」「产品」\n及其属性与关系", color: C.primary },
    { num: "2", title: "约束层", desc: "定义「下单前必须校验库存」\n「已发货订单不可取消」", color: C.orange },
    { num: "3", title: "执行层", desc: "定义「谁可以做什么操作」\n「操作后触发什么事件」", color: C.teal },
  ];

  for (let i = 0; i < 3; i++) {
    const xPos = 0.6 + i * 3.1;
    const lr = layers[i];

    s6.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 3.15, w: 2.8, h: 1.3,
      fill: { color: C.cardBg }, shadow: cardShadow()
    });
    s6.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 3.15, w: 0.06, h: 1.3, fill: { color: lr.color }
    });

    // Number circle
    s6.addShape(pres.shapes.OVAL, {
      x: xPos + 0.2, y: 3.3, w: 0.35, h: 0.35,
      fill: { color: lr.color }
    });
    s6.addText(lr.num, {
      x: xPos + 0.2, y: 3.3, w: 0.35, h: 0.35,
      fontSize: 14, fontFace: "Arial", color: C.white, bold: true, align: "center", valign: "middle", margin: 0
    });

    s6.addText(lr.title, {
      x: xPos + 0.7, y: 3.3, w: 1.9, h: 0.35,
      fontSize: 14, fontFace: "Arial", color: lr.color, bold: true, valign: "middle", margin: 0
    });
    s6.addText(lr.desc, {
      x: xPos + 0.2, y: 3.75, w: 2.4, h: 0.6,
      fontSize: 12, fontFace: "Arial", color: C.textMed, margin: 0
    });
  }

  // Bottom insight
  s6.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 4.65, w: 8.8, h: 0.5,
    fill: { color: C.lightTeal }
  });
  s6.addText("本体之于 AI Agent，如同操作手册之于新员工 —— 告诉你系统怎么运作、规则是什么", {
    x: 0.8, y: 4.65, w: 8.4, h: 0.5,
    fontSize: 13, fontFace: "Arial", color: C.teal, bold: true, align: "center", valign: "middle", margin: 0
  });

  // ========== SLIDE 7: Ontology Role ==========
  let s7 = pres.addSlide();
  s7.background = { color: C.bgDark };

  // Top teal accent bar
  s7.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s7.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s7.addText("02", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 20, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });
  s7.addText("本体模型的角色定位", {
    x: 1.7, y: 0.3, w: 5, h: 0.5,
    fontSize: 20, fontFace: "Arial", color: C.white, bold: true, margin: 0, valign: "middle"
  });

  // Three columns: Agent / Ontology / Business System
  const roles = [
    { title: "AI Agent", subtitle: "意图驱动", items: ["理解自然语言", "生成执行计划", "调用工具完成任务"], icon: "robotW", color: C.teal },
    { title: "本体模型", subtitle: "语义桥梁", items: ["定义业务概念与关系", "约束行为边界", "发布安全工具清单"], icon: "projectW", color: C.orange },
    { title: "业务系统", subtitle: "数据执行", items: ["ERP / CRM / SCM", "数据库 / API", "实际业务操作"], icon: "buildingW", color: C.green },
  ];

  for (let i = 0; i < 3; i++) {
    const xPos = 0.6 + i * 3.1;
    const role = roles[i];

    s7.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 1.2, w: 2.8, h: 3.6,
      fill: { color: C.bgDarkAlt }, rectRadius: 0.1
    });
    s7.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 1.2, w: 2.8, h: 0.06, fill: { color: role.color }
    });

    // Icon
    s7.addImage({ data: icons[role.icon], x: xPos + 1.0, y: 1.5, w: 0.7, h: 0.7 });

    // Title
    s7.addText(role.title, {
      x: xPos, y: 2.35, w: 2.8, h: 0.4,
      fontSize: 18, fontFace: "Arial", color: C.white, bold: true, align: "center", margin: 0
    });

    // Subtitle
    s7.addText(role.subtitle, {
      x: xPos, y: 2.75, w: 2.8, h: 0.3,
      fontSize: 13, fontFace: "Arial", color: role.color, align: "center", margin: 0
    });

    // Items
    for (let j = 0; j < role.items.length; j++) {
      s7.addText(role.items[j], {
        x: xPos + 0.3, y: 3.25 + j * 0.4, w: 2.2, h: 0.35,
        fontSize: 12, fontFace: "Arial", color: C.textLight, margin: 0, valign: "middle"
      });
      s7.addShape(pres.shapes.RECTANGLE, {
        x: xPos + 0.15, y: 3.38 + j * 0.4, w: 0.08, h: 0.08,
        fill: { color: role.color }
      });
    }
  }

  // Arrows between columns
  s7.addShape(pres.shapes.LINE, {
    x: 3.4, y: 3.0, w: 0.3, h: 0,
    line: { color: C.teal, width: 2 }
  });
  s7.addShape(pres.shapes.LINE, {
    x: 6.5, y: 3.0, w: 0.3, h: 0,
    line: { color: C.teal, width: 2 }
  });

  // ========== SLIDE 8: Agent Request Chain ==========
  let s8 = pres.addSlide();
  s8.background = { color: C.white };

  // Top accent bar
  s8.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s8.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s8.addText("03", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 18, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  // Title
  s8.addText("Agent 请求链路：从自然语言到安全执行", {
    x: 0.6, y: 0.9, w: 8.8, h: 0.5,
    fontSize: 22, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // 5-step flow
  const steps = [
    { num: "1", title: "自然语言输入", desc: "用户发出业务请求", color: C.primary },
    { num: "2", title: "意图识别", desc: "LLM 解析用户意图", color: C.teal, tag: "软约束" },
    { num: "3", title: "MCP 工具查询", desc: "获取本体定义的合法操作", color: C.primary, tag: "硬约束" },
    { num: "4", title: "合规校验", desc: "权限 + 聚合根 + 前置条件", color: C.orange, tag: "校验层" },
    { num: "5", title: "安全执行", desc: "调用业务系统 API", color: C.green },
  ];

  for (let i = 0; i < 5; i++) {
    const xPos = 0.4 + i * 1.9;
    const step = steps[i];

    // Step card
    s8.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 1.7, w: 1.7, h: 2.2,
      fill: { color: C.lightFill }, rectRadius: 0.08
    });

    // Number circle
    s8.addShape(pres.shapes.OVAL, {
      x: xPos + 0.55, y: 1.9, w: 0.55, h: 0.55,
      fill: { color: step.color }
    });
    s8.addText(step.num, {
      x: xPos + 0.55, y: 1.9, w: 0.55, h: 0.55,
      fontSize: 18, fontFace: "Arial", color: C.white, bold: true, align: "center", valign: "middle", margin: 0
    });

    // Title
    s8.addText(step.title, {
      x: xPos, y: 2.6, w: 1.7, h: 0.4,
      fontSize: 13, fontFace: "Arial", color: step.color, bold: true, align: "center", margin: 0
    });

    // Description
    s8.addText(step.desc, {
      x: xPos, y: 3.0, w: 1.7, h: 0.5,
      fontSize: 11, fontFace: "Arial", color: C.textMed, align: "center", margin: 0
    });

    // Constraint tag
    if (step.tag) {
      const tagColors = { "软约束": C.teal, "硬约束": C.primary, "校验层": C.orange };
      s8.addShape(pres.shapes.RECTANGLE, {
        x: xPos + 0.4, y: 3.5, w: 0.9, h: 0.28,
        fill: { color: tagColors[step.tag] }, rectRadius: 0.04
      });
      s8.addText(step.tag, {
        x: xPos + 0.4, y: 3.5, w: 0.9, h: 0.28,
        fontSize: 10, fontFace: "Arial", color: C.white, bold: true, align: "center", valign: "middle", margin: 0
      });
    }

    // Arrow to next step
    if (i < 4) {
      s8.addShape(pres.shapes.LINE, {
        x: xPos + 1.7, y: 2.8, w: 0.2, h: 0,
        line: { color: C.textLight, width: 1.5 }
      });
    }
  }

  // Bottom note
  s8.addText("三通道纵深防御：软约束(意图识别) → 硬约束(MCP白名单) → 校验层(执行前验证)", {
    x: 0.6, y: 4.4, w: 8.8, h: 0.4,
    fontSize: 12, fontFace: "Arial", color: C.textMed, align: "center", margin: 0
  });

  // ========== SLIDE 9: MCP vs Skill ==========
  let s9 = pres.addSlide();
  s9.background = { color: C.white };

  // Top accent bar
  s9.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s9.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s9.addText("03", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 18, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  // Title
  s9.addText("为什么选择 MCP 而非 Skill？", {
    x: 0.6, y: 0.9, w: 8.8, h: 0.5,
    fontSize: 22, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // Comparison table
  const tableRows = [
    [
      { text: "维度", options: { fill: { color: C.bgDarkAlt }, color: C.white, bold: true, fontSize: 12, align: "center" } },
      { text: "Skill 方式", options: { fill: { color: C.bgDarkAlt }, color: C.white, bold: true, fontSize: 12, align: "center" } },
      { text: "MCP 方式", options: { fill: { color: C.bgDarkAlt }, color: C.white, bold: true, fontSize: 12, align: "center" } },
    ],
    [
      { text: "工具发现", options: { fontSize: 12, color: C.textDark } },
      { text: "Prompt 注入描述，不可靠", options: { fontSize: 12, color: C.textMed } },
      { text: "结构化工具清单，100% 可发现", options: { fontSize: 12, color: C.teal, bold: true } },
    ],
    [
      { text: "安全约束", options: { fontSize: 12, color: C.textDark } },
      { text: "依赖 Prompt 工程，可绕过", options: { fontSize: 12, color: C.red } },
      { text: "结构化白名单，不可绕过", options: { fontSize: 12, color: C.teal, bold: true } },
    ],
    [
      { text: "参数校验", options: { fontSize: 12, color: C.textDark } },
      { text: "LLM 自行推断，易出错", options: { fontSize: 12, color: C.textMed } },
      { text: "JSON Schema 强校验", options: { fontSize: 12, color: C.teal, bold: true } },
    ],
    [
      { text: "行为边界", options: { fontSize: 12, color: C.textDark } },
      { text: "无硬性限制", options: { fontSize: 12, color: C.red } },
      { text: "聚合根入口 + 前置条件", options: { fontSize: 12, color: C.teal, bold: true } },
    ],
    [
      { text: "版本管理", options: { fontSize: 12, color: C.textDark } },
      { text: "无版本控制", options: { fontSize: 12, color: C.textMed } },
      { text: "Agent Manifest 版本化发布", options: { fontSize: 12, color: C.teal, bold: true } },
    ],
  ];

  s9.addTable(tableRows, {
    x: 0.6, y: 1.6, w: 8.8, h: 3.0,
    colW: [1.5, 3.4, 3.9],
    border: { pt: 0.5, color: C.slate200 },
    rowH: [0.45, 0.45, 0.45, 0.45, 0.45, 0.45],
    autoPage: false,
  });

  // Bottom conclusion
  s9.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 4.7, w: 8.8, h: 0.5,
    fill: { color: C.lightTeal }
  });
  s9.addText("MCP 提供 100% 结构保证的工具白名单 —— 合规是结构必然而非意愿问题", {
    x: 0.8, y: 4.7, w: 8.4, h: 0.5,
    fontSize: 13, fontFace: "Arial", color: C.teal, bold: true, align: "center", valign: "middle", margin: 0
  });

  // ========== SLIDE 10: Four-Layer Architecture Overview ==========
  let s10 = pres.addSlide();
  s10.background = { color: C.white };

  // Top accent bar
  s10.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s10.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s10.addText("04", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 18, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  // Title
  s10.addText("本体模型的四层架构", {
    x: 0.6, y: 0.9, w: 8.8, h: 0.5,
    fontSize: 24, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // Four horizontal layers (bottom to top)
  const archLayers = [
    { name: "治理层", desc: "角色 · 权限 · Agent 沙箱 · 审计", color: C.red, bgColor: "FEF2F2", icon: "lockRed" },
    { name: "事件层", desc: "领域事件 · 集成事件 · 事件处理器 · 事件存储", color: C.orange, bgColor: C.lightOrange, icon: "boltOrange" },
    { name: "动力层", desc: "行为 · 校验规则 · 副作用 · 事务边界 · 指标", color: C.teal, bgColor: C.lightTeal, icon: "robotTeal" },
    { name: "语义层", desc: "限界上下文 · 对象类型 · 属性 · 关系 · 聚合 · 值对象 · 数据源", color: C.primary, bgColor: C.lightBlue, icon: "brainBlue" },
  ];

  for (let i = 0; i < 4; i++) {
    const yPos = 1.6 + i * 0.95;
    const layer = archLayers[i];

    // Layer background
    s10.addShape(pres.shapes.RECTANGLE, {
      x: 0.6, y: yPos, w: 8.8, h: 0.85,
      fill: { color: layer.bgColor }, rectRadius: 0.06
    });

    // Left color bar
    s10.addShape(pres.shapes.RECTANGLE, {
      x: 0.6, y: yPos, w: 0.06, h: 0.85, fill: { color: layer.color }
    });

    // Icon
    s10.addImage({ data: icons[layer.icon], x: 0.85, y: yPos + 0.18, w: 0.45, h: 0.45 });

    // Layer name
    s10.addText(layer.name, {
      x: 1.45, y: yPos + 0.1, w: 1.3, h: 0.65,
      fontSize: 16, fontFace: "Arial", color: layer.color, bold: true, valign: "middle", margin: 0
    });

    // Description
    s10.addText(layer.desc, {
      x: 2.8, y: yPos + 0.1, w: 6.4, h: 0.65,
      fontSize: 13, fontFace: "Arial", color: C.textMed, valign: "middle", margin: 0
    });
  }

  // Bottom note
  s10.addText("限界上下文与本体 1:1 绑定  ·  行为必须通过聚合根入口  ·  声明式发布事件", {
    x: 0.6, y: 4.8, w: 8.8, h: 0.4,
    fontSize: 12, fontFace: "Arial", color: C.teal, align: "center", margin: 0
  });

  // ========== SLIDE 11: Semantic & Dynamic Layer Detail ==========
  let s11 = pres.addSlide();
  s11.background = { color: C.white };

  // Top accent bar
  s11.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s11.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s11.addText("04", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 18, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  // Title
  s11.addText("语义层 & 动力层详解", {
    x: 0.6, y: 0.9, w: 8.8, h: 0.5,
    fontSize: 22, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // Two columns
  // Left: Semantic Layer
  s11.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 1.6, w: 4.2, h: 3.4,
    fill: { color: C.lightBlue }, rectRadius: 0.08
  });
  s11.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 1.6, w: 4.2, h: 0.06, fill: { color: C.primary }
  });

  s11.addText("语义层", {
    x: 0.8, y: 1.75, w: 3.8, h: 0.35,
    fontSize: 16, fontFace: "Arial", color: C.primary, bold: true, margin: 0
  });

  const semItems = [
    "限界上下文 (1:1 绑定本体)",
    "对象类型 · 属性 · 关系类型",
    "聚合 (聚合根) · 值对象",
    "接口抽象 · 数据源",
    "数据获取方式 (SQL/API/MCP)",
    "数据血缘",
  ];
  for (let j = 0; j < semItems.length; j++) {
    s11.addText(semItems[j], {
      x: 0.9, y: 2.25 + j * 0.4, w: 3.7, h: 0.35,
      fontSize: 12, fontFace: "Arial", color: C.textDark, margin: 0, valign: "middle"
    });
    s11.addShape(pres.shapes.RECTANGLE, {
      x: 0.8, y: 2.38 + j * 0.4, w: 0.06, h: 0.06,
      fill: { color: C.primary }
    });
  }

  // Right: Dynamic Layer
  s11.addShape(pres.shapes.RECTANGLE, {
    x: 5.2, y: 1.6, w: 4.2, h: 3.4,
    fill: { color: C.lightTeal }, rectRadius: 0.08
  });
  s11.addShape(pres.shapes.RECTANGLE, {
    x: 5.2, y: 1.6, w: 4.2, h: 0.06, fill: { color: C.teal }
  });

  s11.addText("动力层", {
    x: 5.4, y: 1.75, w: 3.8, h: 0.35,
    fontSize: 16, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  const dynItems = [
    "行为 (核心元语，自由挂载)",
    "校验规则 (前置条件)",
    "副作用配置",
    "事务边界",
    "指标定义",
  ];
  for (let j = 0; j < dynItems.length; j++) {
    s11.addText(dynItems[j], {
      x: 5.5, y: 2.25 + j * 0.4, w: 3.7, h: 0.35,
      fontSize: 12, fontFace: "Arial", color: C.textDark, margin: 0, valign: "middle"
    });
    s11.addShape(pres.shapes.RECTANGLE, {
      x: 5.4, y: 2.38 + j * 0.4, w: 0.06, h: 0.06,
      fill: { color: C.teal }
    });
  }

  // Key insight
  s11.addShape(pres.shapes.RECTANGLE, {
    x: 5.4, y: 4.15, w: 3.8, h: 0.55,
    fill: { color: C.white }
  });
  s11.addText("「行为」不分类 —— 行为是唯一元语，\n自由挂载到聚合根", {
    x: 5.5, y: 4.15, w: 3.6, h: 0.55,
    fontSize: 11, fontFace: "Arial", color: C.teal, bold: true, margin: 4
  });

  // ========== SLIDE 12: Governance & Agent Manifest ==========
  let s12 = pres.addSlide();
  s12.background = { color: C.white };

  // Top accent bar
  s12.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s12.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s12.addText("04", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 18, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  // Title
  s12.addText("治理层 & Agent Manifest", {
    x: 0.6, y: 0.9, w: 8.8, h: 0.5,
    fontSize: 22, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // Left: Governance
  s12.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 1.6, w: 4.2, h: 3.2,
    fill: { color: "FEF2F2" }, rectRadius: 0.08
  });
  s12.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 1.6, w: 4.2, h: 0.06, fill: { color: C.red }
  });

  s12.addText("治理层", {
    x: 0.8, y: 1.75, w: 3.8, h: 0.35,
    fontSize: 16, fontFace: "Arial", color: C.red, bold: true, margin: 0
  });

  const govItems = [
    "角色定义与权限分配",
    "对象级权限 (行级访问控制)",
    "字段级权限 (敏感字段脱敏)",
    "条件权限 (上下文感知授权)",
    "AI Agent 沙箱 (隔离执行环境)",
  ];
  for (let j = 0; j < govItems.length; j++) {
    s12.addText(govItems[j], {
      x: 0.9, y: 2.25 + j * 0.45, w: 3.7, h: 0.4,
      fontSize: 12, fontFace: "Arial", color: C.textDark, margin: 0, valign: "middle"
    });
    s12.addShape(pres.shapes.RECTANGLE, {
      x: 0.8, y: 2.4 + j * 0.45, w: 0.06, h: 0.06,
      fill: { color: C.red }
    });
  }

  // Right: Agent Manifest
  s12.addShape(pres.shapes.RECTANGLE, {
    x: 5.2, y: 1.6, w: 4.2, h: 3.2,
    fill: { color: C.lightFill }, rectRadius: 0.08
  });
  s12.addShape(pres.shapes.RECTANGLE, {
    x: 5.2, y: 1.6, w: 4.2, h: 0.06, fill: { color: C.teal }
  });

  s12.addText("Agent Manifest", {
    x: 5.4, y: 1.75, w: 3.8, h: 0.35,
    fontSize: 16, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  s12.addText("本体编译后的版本化发布结构", {
    x: 5.4, y: 2.15, w: 3.8, h: 0.3,
    fontSize: 12, fontFace: "Arial", color: C.textMed, margin: 0
  });

  // Manifest structure as code-like block
  s12.addShape(pres.shapes.RECTANGLE, {
    x: 5.4, y: 2.6, w: 3.8, h: 2.0,
    fill: { color: C.bgDarkAlt }
  });
  s12.addText([
    { text: "{", options: { color: C.textLight, fontSize: 11, fontFace: "Consolas", breakLine: true } },
    { text: '  "version": "1.2.0",', options: { color: C.teal, fontSize: 11, fontFace: "Consolas", breakLine: true } },
    { text: '  "tools": [...],', options: { color: C.teal, fontSize: 11, fontFace: "Consolas", breakLine: true } },
    { text: '  "permissions": [...],', options: { color: C.teal, fontSize: 11, fontFace: "Consolas", breakLine: true } },
    { text: '  "constraints": [...],', options: { color: C.teal, fontSize: 11, fontFace: "Consolas", breakLine: true } },
    { text: "}", options: { color: C.textLight, fontSize: 11, fontFace: "Consolas" } },
  ], { x: 5.5, y: 2.7, w: 3.6, h: 1.8, margin: 4, valign: "top" });

  // ========== SLIDE 13: Lifecycle Iteration ==========
  let s13 = pres.addSlide();
  s13.background = { color: C.white };

  // Top accent bar
  s13.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s13.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s13.addText("05", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 18, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  // Title
  s13.addText("本体模型的全生命周期迭代", {
    x: 0.6, y: 0.9, w: 8.8, h: 0.5,
    fontSize: 22, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // 4 phases in a horizontal flow
  const phases = [
    { title: "建模", desc: "业务专家定义\n概念与关系", color: C.primary, bgColor: C.lightBlue },
    { title: "验证", desc: "规则一致性\n与完整性检查", color: C.teal, bgColor: C.lightTeal },
    { title: "发布", desc: "编译为 Agent\nManifest 快照", color: C.orange, bgColor: C.lightOrange },
    { title: "演进", desc: "版本化管理\n持续适配迭代", color: C.green, bgColor: C.lightGreen },
  ];

  for (let i = 0; i < 4; i++) {
    const xPos = 0.4 + i * 2.4;
    const ph = phases[i];

    s13.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 1.6, w: 2.1, h: 1.8,
      fill: { color: ph.bgColor }, rectRadius: 0.08
    });
    s13.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 1.6, w: 2.1, h: 0.06, fill: { color: ph.color }
    });

    // Phase number
    s13.addText(String(i + 1), {
      x: xPos + 0.1, y: 1.75, w: 0.4, h: 0.35,
      fontSize: 18, fontFace: "Arial", color: ph.color, bold: true, margin: 0
    });

    s13.addText(ph.title, {
      x: xPos + 0.5, y: 1.75, w: 1.4, h: 0.35,
      fontSize: 16, fontFace: "Arial", color: ph.color, bold: true, valign: "middle", margin: 0
    });

    s13.addText(ph.desc, {
      x: xPos + 0.15, y: 2.25, w: 1.8, h: 0.9,
      fontSize: 12, fontFace: "Arial", color: C.textMed, margin: 0
    });

    // Arrow
    if (i < 3) {
      s13.addShape(pres.shapes.LINE, {
        x: xPos + 2.1, y: 2.5, w: 0.3, h: 0,
        line: { color: C.textLight, width: 1.5 }
      });
    }
  }

  // 4 strategies below
  s13.addText("四大迭代策略", {
    x: 0.6, y: 3.6, w: 8.8, h: 0.35,
    fontSize: 14, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  const strategies = [
    { name: "增量扩展", desc: "新增概念不影响已有定义" },
    { name: "兼容升级", desc: "新版本向后兼容旧 Agent" },
    { name: "废弃标记", desc: "旧概念标记废弃而非删除" },
    { name: "版本并行", desc: "多版本 Manifest 同时服务" },
  ];

  for (let i = 0; i < 4; i++) {
    const xPos = 0.6 + i * 2.3;
    const st = strategies[i];

    s13.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 4.05, w: 2.1, h: 1.1,
      fill: { color: C.lightFill }, rectRadius: 0.06
    });

    s13.addText(st.name, {
      x: xPos + 0.15, y: 4.1, w: 1.8, h: 0.35,
      fontSize: 13, fontFace: "Arial", color: C.teal, bold: true, margin: 0
    });
    s13.addText(st.desc, {
      x: xPos + 0.15, y: 4.5, w: 1.8, h: 0.5,
      fontSize: 11, fontFace: "Arial", color: C.textMed, margin: 0
    });
  }

  // ========== SLIDE 14: Enterprise Operations ==========
  let s14 = pres.addSlide();
  s14.background = { color: C.white };

  // Top accent bar
  s14.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Chapter label
  s14.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.3, w: 0.06, h: 0.5, fill: { color: C.teal } });
  s14.addText("06", {
    x: 0.8, y: 0.3, w: 0.8, h: 0.5,
    fontSize: 18, fontFace: "Arial", color: C.teal, bold: true, margin: 0
  });

  // Title
  s14.addText("五大运维支柱 · 多租户架构", {
    x: 0.6, y: 0.9, w: 8.8, h: 0.5,
    fontSize: 22, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // 5 pillars
  const pillars = [
    { name: "监控", desc: "Agent 调用链路\n实时监控", color: C.primary, bgColor: C.lightBlue },
    { name: "日志", desc: "结构化审计日志\n全量可追溯", color: C.teal, bgColor: C.lightTeal },
    { name: "追踪", desc: "请求链路跨系统\n端到端追踪", color: C.orange, bgColor: C.lightOrange },
    { name: "告警", desc: "异常行为实时\n告警与熔断", color: C.red, bgColor: "FEF2F2" },
    { name: "多租户", desc: "数据隔离 +\n权限隔离", color: C.green, bgColor: C.lightGreen },
  ];

  for (let i = 0; i < 5; i++) {
    const xPos = 0.4 + i * 1.9;
    const pl = pillars[i];

    s14.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 1.6, w: 1.7, h: 2.0,
      fill: { color: pl.bgColor }, rectRadius: 0.08
    });
    s14.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 1.6, w: 1.7, h: 0.06, fill: { color: pl.color }
    });

    s14.addText(pl.name, {
      x: xPos, y: 1.85, w: 1.7, h: 0.4,
      fontSize: 15, fontFace: "Arial", color: pl.color, bold: true, align: "center", margin: 0
    });
    s14.addText(pl.desc, {
      x: xPos + 0.1, y: 2.4, w: 1.5, h: 0.9,
      fontSize: 12, fontFace: "Arial", color: C.textMed, align: "center", margin: 0
    });
  }

  // Bottom: 国产化 support
  s14.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 3.9, w: 8.8, h: 1.0,
    fill: { color: C.lightFill }, rectRadius: 0.08
  });
  s14.addText("国产化适配", {
    x: 0.8, y: 4.0, w: 2, h: 0.35,
    fontSize: 14, fontFace: "Arial", color: C.primary, bold: true, margin: 0
  });
  s14.addText("支持国产数据库（达梦 / 人大金仓）· 国产操作系统（麒麟 / 统信）· 信创合规", {
    x: 0.8, y: 4.4, w: 8.4, h: 0.35,
    fontSize: 12, fontFace: "Arial", color: C.textMed, margin: 0
  });

  // ========== SLIDE 15: Product Roadmap ==========
  let s15 = pres.addSlide();
  s15.background = { color: C.white };

  // Top accent bar
  s15.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Title
  s15.addText("产品路线图", {
    x: 0.6, y: 0.3, w: 8.8, h: 0.5,
    fontSize: 24, fontFace: "Arial", color: C.textDark, bold: true, margin: 0
  });

  // Timeline - 4 milestones
  const milestones = [
    { phase: "MVP", time: "Q3 2026", items: ["本体建模核心", "MCP 工具发布", "基础权限控制"], color: C.teal },
    { phase: "V1.5", time: "Q4 2026", items: ["可视化建模器", "版本管理", "Agent 运行监控"], color: C.primary },
    { phase: "V2.0", time: "Q1 2027", items: ["CQRS 投影", "多租户完善", "国产化适配"], color: C.orange },
    { phase: "V2.5+", time: "2027+", items: ["行业模板库", "AI 辅助建模", "生态开放"], color: C.green },
  ];

  // Timeline line
  s15.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 1.7, w: 8.8, h: 0.04, fill: { color: C.slate200 }
  });

  for (let i = 0; i < 4; i++) {
    const xPos = 0.6 + i * 2.35;
    const ms = milestones[i];

    // Timeline dot
    s15.addShape(pres.shapes.OVAL, {
      x: xPos + 0.8, y: 1.58, w: 0.25, h: 0.25,
      fill: { color: ms.color }
    });

    // Phase label
    s15.addText(ms.phase, {
      x: xPos, y: 1.15, w: 2.1, h: 0.35,
      fontSize: 18, fontFace: "Arial", color: ms.color, bold: true, align: "center", margin: 0
    });
    s15.addText(ms.time, {
      x: xPos, y: 0.85, w: 2.1, h: 0.3,
      fontSize: 12, fontFace: "Arial", color: C.textMed, align: "center", margin: 0
    });

    // Items card below timeline
    s15.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: 2.1, w: 2.1, h: 1.6,
      fill: { color: C.lightFill }, rectRadius: 0.06
    });

    for (let j = 0; j < ms.items.length; j++) {
      s15.addText(ms.items[j], {
        x: xPos + 0.15, y: 2.25 + j * 0.42, w: 1.8, h: 0.38,
        fontSize: 12, fontFace: "Arial", color: C.textDark, margin: 0, valign: "middle"
      });
      s15.addShape(pres.shapes.RECTANGLE, {
        x: xPos + 0.08, y: 2.39 + j * 0.42, w: 0.05, h: 0.05,
        fill: { color: ms.color }
      });
    }
  }

  // Bottom CTA
  s15.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 4.2, w: 8.8, h: 0.5,
    fill: { color: C.lightTeal }
  });
  s15.addText("从 MVP 到生态开放，持续构建 Agentic AI 的业务语义基础设施", {
    x: 0.8, y: 4.2, w: 8.4, h: 0.5,
    fontSize: 13, fontFace: "Arial", color: C.teal, bold: true, align: "center", valign: "middle", margin: 0
  });

  // ========== SLIDE 16: Summary ==========
  let s16 = pres.addSlide();
  s16.background = { color: C.bgDark };

  // Top teal accent bar
  s16.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 0.06, fill: { color: C.teal } });

  // Title
  s16.addText("总结", {
    x: 0.6, y: 0.3, w: 8.8, h: 0.6,
    fontSize: 36, fontFace: "Arial", color: C.white, bold: true, margin: 0
  });

  // 6 key points in 2x3 grid
  const summaryPoints = [
    { text: "本体是 AI 时代的业务知识工程", color: C.teal },
    { text: "四层架构：语义 · 动力 · 事件 · 治理", color: C.primary },
    { text: "MCP 提供 100% 结构保证", color: C.orange },
    { text: "三通道纵深防御确保合规", color: C.red },
    { text: "Agent Manifest 版本化发布", color: C.green },
    { text: "全生命周期迭代适配企业需求", color: C.teal },
  ];

  for (let i = 0; i < 6; i++) {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const xPos = 0.6 + col * 4.6;
    const yPos = 1.2 + row * 1.25;
    const pt = summaryPoints[i];

    s16.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: yPos, w: 4.2, h: 1.0,
      fill: { color: C.bgDarkAlt }, rectRadius: 0.08
    });
    s16.addShape(pres.shapes.RECTANGLE, {
      x: xPos, y: yPos, w: 0.06, h: 1.0, fill: { color: pt.color }
    });

    s16.addText(pt.text, {
      x: xPos + 0.2, y: yPos, w: 3.8, h: 1.0,
      fontSize: 15, fontFace: "Arial", color: C.white, bold: true, valign: "middle", margin: 0
    });
  }

  // Bottom tagline
  s16.addText("让 AI Agent 100% 理解并安全执行业务操作", {
    x: 0.6, y: 4.7, w: 8.8, h: 0.5,
    fontSize: 16, fontFace: "Arial", color: C.teal, bold: true, align: "center", valign: "middle", margin: 0
  });

  // Bottom line
  s16.addShape(pres.shapes.RECTANGLE, { x: 0, y: 5.2, w: 10, h: 0.02, fill: { color: C.teal } });

  // ========== Write File ==========
  const outputPath = "E:\\00 - AI\\本体建模\\本体建模平台-产品介绍-优化版.pptx";
  await pres.writeFile({ fileName: outputPath });
  console.log("PPT generated: " + outputPath);
  console.log("Total slides: 16");
}

main().catch(err => {
  console.error("Error:", err.message);
  console.error(err.stack);
  process.exit(1);
});
