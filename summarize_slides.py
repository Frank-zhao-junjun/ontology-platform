import sys, json
sys.path.insert(0, 'C:/Users/Frank/.workbuddy/binaries/python/versions/3.13.12/Lib/site-packages')

with open('E:/00 - AI/本体建模/pptx_analysis.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

lines = []
for slide in data['slides']:
    n = slide['slide_number']
    lines.append(f"\n{'='*60}")
    lines.append(f"SLIDE {n}")
    lines.append(f"{'='*60}")
    
    all_texts = []
    font_sizes = []
    colors_used = []
    image_count = 0
    shape_count = 0
    has_textbox = False
    
    for shape in slide['shapes']:
        shape_count += 1
        if 'is_image' in shape and shape['is_image']:
            image_count += 1
        if 'fill_color' in shape:
            colors_used.append(shape['fill_color'])
        if 'line_color' in shape:
            colors_used.append(shape['line_color'])
        if shape['shape_type'] == 'TEXT_BOX (17)':
            has_textbox = True
        if 'text_frame' in shape:
            for para in shape['text_frame']['paragraphs']:
                text = para['text'].strip()
                if text:
                    all_texts.append(text)
                    for run in para.get('runs', []):
                        if 'size_pt' in run:
                            font_sizes.append(run['size_pt'])
                        if 'color' in run:
                            colors_used.append(run['color'])
    
    lines.append(f"Shapes: {shape_count} | Images: {image_count} | Uses TextBox: {has_textbox}")
    unique_colors = list(set(colors_used))
    lines.append(f"Colors: {', '.join(unique_colors)}")
    if font_sizes:
        lines.append(f"Font sizes (pt): min={min(font_sizes):.0f}, max={max(font_sizes):.0f}, unique={sorted(set(font_sizes))}")
    lines.append(f"Text content ({len(all_texts)} items):")
    for t in all_texts:
        lines.append(f"  - {repr(t)}")

output_path = 'E:/00 - AI/本体建模/slides_summary.txt'
with open(output_path, 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))
print(f"Summary written to {output_path}")
