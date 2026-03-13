import os
import re

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if "System.out.println" not in content:
        return

    print(f"Processing {filepath}")
    
    # Add import if needed
    if "import org.slf4j.Logger;" not in content:
        content = re.sub(r'(package [^;]+;)', r'\1\n\nimport org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;', content, count=1)
    
    # Add Logger instance
    class_name_match = re.search(r'(?:public |protected |private |)class\s+(\w+)', content)
    if class_name_match:
        class_name = class_name_match.group(1)
        if "Logger logger =" not in content and "Logger LOGGER =" not in content:
            content = re.sub(r'((?:public |protected |private |)class\s+' + class_name + r'.*?\{)', 
                           r'\1\n    private static final Logger logger = LoggerFactory.getLogger(' + class_name + '.class);', 
                           content, count=1)
                           
    # Replace System.out.println
    content = re.sub(r'System\.out\.println\((.*?)\);', r'logger.info(\1);', content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for root, _, files in os.walk('d:/python/e-commerce-app/src'):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file))
