from PIL import Image

img_path = "app/src/main/assets/games/images/father.png"
img = Image.open(img_path)
bbox = img.getbbox()
if bbox:
    img.crop(bbox).save(img_path)
    print(f"Trimmed: {img.size} -> {img.crop(bbox).size}")
