"""Stage 2 of the rename: propagate the five renamed mobs into every OTHER piece of Alex's Caves
content whose name or prose derives from them, in all 13 locales, inside the compat pack only.

Why this exists
---------------
tools/localize_names.py renames the five Primordial Caves dinosaurs themselves (entity, egg block,
spawn egg). But with the full Alex's Caves installed there is a whole tail of content that NAMES
them and would keep showing Alex's Caves' names next to ours:

  * the Tremorzilla -- a Tremorsaurus derivative living in the Toxic Caves. Roarer + zilla =
    **Roarerzilla**: entity, spawn egg, egg block, its advancements and its guide entries.
  * every sound subtitle ("Grottoceratops groans loudly"),
  * the cave-painting descriptions ("Befriended Tremorsaurus"),
  * the Extinction Spear enchantment descriptions ("Extinction Spear Tremorsaurus attack ..."),
  * the advancement descriptions ("Breed Grottoceratops with Tree Stars"),
  * and the guide book prose (assets/alexscaves/books/<locale>/**.txt).

The Atlatitan is deliberately untouched -- its name derives from none of the five, so nothing here
matches it. The Luxtructosaurus keeps its own name too; only its MENTIONS of the five are renamed.

Everything is written to resourcepacks/primordial_compat, which is required + top priority when
Alex's Caves is installed, and is dead weight otherwise: none of this content exists standalone.

Run AFTER tools/localize_names.py (that one rebuilds the pack's lang files from scratch):
    python3 tools/localize_names.py && python3 tools/derived_names.py
"""
import json
import os
import re
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
PACK = os.path.join(PROJ, 'src/main/resources/resourcepacks/primordial_compat/assets/alexscaves')
AC_JAR = os.path.join(PROJ, 'libs/alexscaves-full-2.0.2.jar')

# Alex's Caves entity id -> the Primordial Mobs name. The first five are the mod's own renames; the
# Tremorzilla is included because it is a NAME derivative of the Tremorsaurus (Roarer), so wherever
# a locale writes its own "Tremorzilla" we write Roarerzilla.
MOBS = {'grottoceratops': 'Rammer', 'relicheirus': 'Logger', 'tremorsaurus': 'Roarer',
        'subterranodon': 'Glider', 'vallumraptor': 'Stealer', 'tremorzilla': 'Roarerzilla'}

# Keys stage 1 already writes; re-deriving them here would fight it. Only the FIVE renamed mobs --
# the Tremorzilla is not stage 1's business, so its egg block and spawn egg must be derived here.
STAGE1_MOBS = [m for m in MOBS if m != 'tremorzilla']
STAGE1 = {'entity.alexscaves.%s' % m for m in STAGE1_MOBS} | \
         {'block.alexscaves.%s_egg' % m for m in STAGE1_MOBS} | \
         {'item.alexscaves.spawn_egg_%s' % m for m in STAGE1_MOBS}

# Locales that decline the creature name inside a sentence. Exact-substring matching misses those
# ("Яйцо сотрясозиллы", "Jajo tremorzilli", "Приручіть тремозіллу"), so for these we match on the
# name minus its last character and swallow whatever lowercase ending follows. A Cyrillic or Polish
# case ending glued to a Latin name reads wrong ("Яйцо Roarerzillaы"), so it is dropped either way.
INFLECTED = {'ru_ru', 'uk_ua', 'pl_pl'}
INFLECTION_TAIL = re.compile(r'^[a-zа-яёіїєґąćęłńóśźż́]+')
MIN_STEM = 5


def match_forms(locale, ac_name):
    """The strings to look for: the name itself, plus its stem when the locale inflects it."""
    forms = [ac_name]
    if locale in INFLECTED and len(ac_name) - 1 >= MIN_STEM:
        forms.append(ac_name[:-1])
    return forms


def substitute_all(locale, text, pairs):
    """Replace every occurrence of each localized creature name in `text` with the mod name.

    `pairs` is ordered longest-name-first so that a name which contains another
    (e.g. zh_cn 撼地龙 / 撼地斯拉) cannot be partially eaten by the shorter one.
    """
    out = text
    for ac_name, english in pairs:
        for form in match_forms(locale, ac_name):
            i = 0
            while True:
                j = out.lower().find(form.lower(), i)
                if j < 0:
                    break
                head, tail = out[:j], out[j + len(form):]
                if locale in INFLECTED:
                    tail = INFLECTION_TAIL.sub('', tail)
                out = head + english + tail
                i = j + len(english)
    return out


def name_pairs(ac_lang, ac_en):
    """Localized Alex's Caves name -> our name, longest first, deduplicated."""
    pairs = []
    for mob, english in MOBS.items():
        key = 'entity.alexscaves.%s' % mob
        for candidate in (ac_lang.get(key), ac_en.get(key)):
            if candidate and (candidate, english) not in pairs:
                pairs.append((candidate, english))
    # Longest first: in zh_cn the Tremorsaurus is 撼地龙 and the Tremorzilla 撼地斯拉; substituting
    # the shorter one first would leave "Roarer斯拉" instead of Roarerzilla.
    pairs.sort(key=lambda p: -len(p[0]))
    return pairs


def main():
    jar = zipfile.ZipFile(AC_JAR)
    ac_en = json.loads(jar.read('assets/alexscaves/lang/en_us.json'))
    lang_dir = os.path.join(PACK, 'lang')
    locales = sorted(f[:-5] for f in os.listdir(lang_dir) if f.endswith('.json'))

    total_keys = 0
    for locale in locales:
        try:
            ac = json.loads(jar.read('assets/alexscaves/lang/%s.json' % locale))
        except KeyError:
            ac = dict(ac_en)                      # e.g. tok: Alex's Caves ships no such locale
        pairs = name_pairs(ac, ac_en)

        path = os.path.join(lang_dir, '%s.json' % locale)
        data = json.load(open(path, encoding='utf-8'))
        added = 0
        for key, ac_value in ac.items():
            if key in STAGE1 or not isinstance(ac_value, str):
                continue
            new = substitute_all(locale, ac_value, pairs)
            if new != ac_value and data.get(key) != new:
                data[key] = new
                added += 1
        # The Tremorzilla's own name is a proper noun of the mod, like the other five.
        if data.get('entity.alexscaves.tremorzilla') != 'Roarerzilla':
            data['entity.alexscaves.tremorzilla'] = 'Roarerzilla'
            added += 1
        with open(path, 'w', encoding='utf-8') as fh:
            json.dump(dict(sorted(data.items())), fh, ensure_ascii=False, indent=2)
            fh.write('\n')
        total_keys += added
        print('%s: %d derived keys' % (locale, added))

    # ---- guide book prose -------------------------------------------------------------------
    book_files = [n for n in jar.namelist()
                  if n.startswith('assets/alexscaves/books/') and n.endswith('.txt')]
    written = 0
    for name in book_files:
        rel = name[len('assets/alexscaves/books/'):]        # <locale>/<chapter>/<entry>.txt
        locale = rel.split('/')[0]
        try:
            ac = json.loads(jar.read('assets/alexscaves/lang/%s.json' % locale))
        except KeyError:
            ac = dict(ac_en)
        pairs = name_pairs(ac, ac_en)
        text = jar.read(name).decode('utf-8')
        new = substitute_all(locale, text, pairs)
        if new == text:
            continue
        out = os.path.join(PACK, 'books', rel)
        os.makedirs(os.path.dirname(out), exist_ok=True)
        with open(out, 'w', encoding='utf-8') as fh:
            fh.write(new)
        written += 1
    print('%d derived lang keys, %d guide pages rewritten' % (total_keys, written))


if __name__ == '__main__':
    main()
