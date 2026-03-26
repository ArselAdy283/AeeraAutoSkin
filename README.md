<h1 align="center">Aeera Auto SKin</h1>

<p align="center">
Plugin Minecraft untuk kebutuhan roleplay yang memungkinkan pemain mengganti skin dengan cepat
seperti mengganti baju menggunakan item khusus (item baju).
</p>

## Fitur
- Ganti skin langsung menggunakan item (seperti baju)
- Upload skin pakai website
- Edit skin lewat webiste tanpa restart server (Edit = hapus terus upload ulang 😹)

## Cara Pakai

### 1. Setup API Skin
- Gunakan repository ini:  
  <a href="https://github.com/ArselAdy283/upload-skin-aeera.git">upload-skin-aeera<a>  
- Deploy project itu
- Input data:
  - `jenis_baju`
  - `skins`

### 2. Setup Item
- Buat item di Minecraft
- Tambahkan lore sesuai dengan `jenis_baju` dari API

### 3. Install SkinsRestorer
- Download: https://skinsrestorer.net/  
- Edit `config.yml` menjadi:

```yml
messages:
  locale: silent
  disablePrefix: true

server:
  sound:
    value: ITEM_ARMOR_EQUIP_LEATHER, 0.7
```

### 4. Setup Silent Messages

Tambahkan file berikut:

`/plugins/SkinsRestorer/locale/custom/messages_silent.json`

```json
{
  "skinsrestorer.prefix_format": "",
  "skinsrestorer.success_generic": "",
  "skinsrestorer.error_generic": ""
}
```

Catatan: tidak semua key wajib diisi, cukup yang ingin dihilangkan tapi bagusan semua.
