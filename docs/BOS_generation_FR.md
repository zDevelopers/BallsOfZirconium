# Outils de génération pour _Balls of Zirconium_

Toutes les listes de blocs sont au format WorldEdit (par exemple, `stone:4` ou `40%prismarine:1,60%sea_lantern`).

Les règles de génération sont stockées dans un dossier situé dans le dossier du plugin. Ce dossier doit avoir la structure suivante : 

```
|- map_config
   |- map.yml
   |- schematics
      |- building.schematic
      |- sphere.schematic
   |- loot_tables
      |- loot.json
      |- other_loot.json
```

Le nom du dossier (ici `map_config`) est libre et doit être référencé dans le fichier de configuration (clef `map_config`). Les dossiers `schematics` et `loot_tables` ne peuvent voir leurs noms modifiés, mais peuvent être omis s'ils sont vides. Ils peuvent contenir des sous-dossiers pour organiser les fichiers. Le fichier définissant les règles de génération doit s'appeler `map.yml`.


## Règles de génération

Des règles de génération pour les sphères (ou pas sphères d'ailleurs).

Technique : classe implémentant `eu.carrade.amaury.BallsOfSteel.generators.Generator`, qu'a un constructeur prenant une `Map` d'arguments en constructeur et qui dispose d'une méthode `generate(EditSession session, Vector base, Random random)` pour générer. La source d'aléatoire doit être utilisée autant que faire se peut pour la génération, afin d'avoir une génération cohérente en fonction de la graine du monde.

La classe est trouvée à partir du nom, en tentant de charger : 
- `eu.carrade.amaury.BallsOfSteel.generators.<nom>` ;
- `eu.carrade.amaury.BallsOfSteel.generators.<nom>Generator` ;
- `eu.carrade.amaury.BallsOfSteel.generators.<nomCapitalisé>` ;
- `eu.carrade.amaury.BallsOfSteel.generators.<nomCapitalisé>Generator` ;
- le nom brut comme nom complet de classe (afin de pouvoir utiliser d'éventuels générateurs externes).


 


- **Paramètres communs à tous les générateurs**  
  Ces paramètres peuvent être ajoutés à tous les générateurs ci-dessous.
	- `enabled` : booléen (signification évidente).
	- `probability` : nombre entre 0 et 1. Si fourni, la génération sera effectuée suivant cette probabilité.
	- `offset` : un décalage (vecteur) du point central de génération pour cette règle. Si non fourni, la génération se fera centrée sur le point de génération de la sphère. Avec, le point de référence sera décalé.

- **`schematic`**  
  Colle un schematic (en prenant comme point de collage le point central de génération).
	- `schematic` : chemin vers le schematic à coller. Le schematic est cherché dans le sous-dossier `schematics` du dossier de la définition de la carte (voir introduction).

- **`sphere`**  
  Génère une sphère.
	- `size` : le rayon de la sphère.
	- `content` : le contenu de la sphère (format WorldEdit).

- **`hsphere`**  
  Génère une sphère creuse.
	- `size` : le rayon de la sphère.
	- `content` : le contenu de la sphère (format WorldEdit).

- **`cylinder`**  
  Génère un cylindre.
	- `size` : le rayon du cylindre.
	- `content` : le contenu du cylindre (format WorldEdit).

- **`hcylinder`**  
  Génère un cylindre creu.
	- `size` : le rayon du cylindre.
	- `content` : le contenu du cylindre (format WorldEdit).

- **`pyramid`**  
  Génère une pyramide.
	- `size` : le côté de la pyramide.
	- `content` : le contenu de la pyramide (format WorldEdit).

- **`hpyramid`**  
  Génère une pyramide creuse.
	- `size` : le côté de la pyramide.
	- `content` : le contenu de la pyramide (format WorldEdit).

- **`walls`**  
  Génère les murs d'un cube centré sur le point de génération.
	- `size` : la taille du cuboïde (vecteur : x,y,z).
	- `content` : le contenu du mur (format WorldEdit).

- **`cuboidFaces`**  
  Génère les faces d'un cuboïde (comme walls, mais avec le sol et le plafond).
	- `size` : la taille du cuboïde (vecteur : x,y,z).
	- `content` : le contenu des faces (format WorldEdit).


## Post-traitements

Des règles de post-traitement, appliquées sur une sélection après qu'une sphère ait été générée, collée, ou qu'un bâtiment statique ait été collé.

Technique : classe implémentant `eu.carrade.amaury.BallsOfSteel.postProcessing.PostProcessing`, qu'a un constructeur prenant une `Map` d'arguments en constructeur et qui dispose d'une méthode `proccess(EditSession session, Region region, Random random)` pour appliquer le traitement. Les deux arguments sont fournis car la région peut différer de la zone traitée par la session d'édition (cf. paramètres communs). La source d'aléatoire doit être utilisée autant que faire se peut pour la génération, afin d'avoir une génération cohérente en fonction de la graine du monde.


La classe est trouvée à partir du nom, en tentant de charger : 
- `eu.carrade.amaury.BallsOfSteel.postProcessing.<nom>` ;
- `eu.carrade.amaury.BallsOfSteel.postProcessing.<nom>PostProcessor` ;
- `eu.carrade.amaury.BallsOfSteel.postProcessing.<nomCapitalisé>` ;
- `eu.carrade.amaury.BallsOfSteel.postProcessing.<nomCapitalisé>PostProcessor` ;
- le nom brut comme nom complet de classe (afin de pouvoir utiliser d'éventuels générateurs externes).


 


- **Paramètres communs à tous les traitements**  
  Ces paramètres peuvent être ajoutés à tous les traitements ci-dessous.
	- `enabled` : booléen (signification évidente).
	- `region` : limite la région affectée par ce traitement à un cuboïde ayant les coordonnées précisées aux coins.
	    - Les coordonnées sont spécifiées dans les sous-clefs `sub_pos_1` et `sub_pos_2`.
	    - Les coordonnées doivent être relatives au plus petit point de la sélection (le coin aux coordonnées les plus basses) (`Region.getMinimumPoint()` techniquement). Ainsi pour couvrir un petit cube dans le coin aux plus petites coordonnées, il faut passer `0,0,0` et par exemple `3,3,3` respectivement.
	    - Les coordonnées ne doivent ainsi pas être négatives.
	    - Quelque soit la taille de la boîte précisée ici, elle sera limitée à la taille de la structure générée.
	    - Si non précisé, le traitement sera effectué sur l'intégralité de la zone.
	- `probability` : nombre entre 0 et 1. Si fourni, le traitement sera appliqué suivant cette probabilité.

- **`replace`**  
  Fait un remplacement.
	- `from` : bloc(s) replacés
	- `to` : bloc(s) nouveaux

- **`replaceVisibleBlocks`**  
  Remplace les blocs visibles (ayant au moins un bloc transparent accolé).
	- `from` : bloc(s) replacés s'ils sont visibles
	- `to` : bloc(s) remplaçant les blocs visibles correspondant au `from`

- **`set`**  
  Modifie tous les blocs de la sélection.
	- `block`: bloc(s) à mettre

- **`flip`**  
  Effectue un miroir de la sélection.
	- `direction` : la direction d'inversion. Peut être une direction donnée (`up`, `down`, `east`, `west`, `north`, `south`) ou un paramètre aléatoire : `random` (direction complètement aléatoire), `random_horizontal` (mirroir aléatoire mais uniquement horizontal) ou `random_vertical` (la même mais vertical). Dans le cas de l'utilisation d'un paramètre aléatoire, il se peut qu'aucune transformation ne soit effectuée.

- **`rotate`**  
  Tourne la sélection.
	- `angle` : l'angle de rotation (`0`, `90`, `180`, `270`), ou `random` pour en sélectionner un aléatoirement.

- **`populateChests`**  
  Peuple les coffres d'éléments, aléatoirement. Cette règle va peupler les conteneurs trouvés dans la zone d'application (sauf si explicitement désactivé pour certains conteneurs). Minecraft ne le supportant pas, le remplissage basé sur des _loot tables_ de coffres d'entités (mules, lamas...) n'est actuellement pas supporté. Ceci pourrait évoluer dans le futur.  
  Note : certains conteneurs qui le devraient (et qui sont listés ci-dessous) [ne supportent pas les _loot tables_ suite à un bug](https://bugs.mojang.com/browse/MC-93486).
	- `only_empty` : booléen. Ne remplie que les coffres vides si actif. Sinon, vide tous les coffres et applique la règle de génération fournie.
	- `chests` : booléen. Si `false`, les coffres simples de la sélection ne seront pas impactés. Par défaut `true`.
	- `trapped_chests` : booléen. Si `false`, les coffres piégés de la sélection ne seront pas impactés. Par défaut `true`.
	- `shulker_boxes` : booléen. Si `false`, les boîtes de Shulker de la sélection ne seront pas impactés. Par défaut `true`.
	- `hoppers` : booléen. Si `false`, les entonnoirs de la sélection ne seront pas impactés. Par défaut `true`.
	- `dispensers` : booléen. Si `false`, les distributeurs de la sélection ne seront pas impactés. Par défaut `true`.
	- `droppers` : booléen. Si `false`, les droppers de la sélection ne seront pas impactés. Par défaut `true`.
	- `furnaces` : booléen. Si `false`, les fours de la sélection ne seront pas impactés. Par défaut `true`.
	- `storage_minecarts` : booléen. Si `false`, les minecarts avec coffre de la sélection ne seront pas impactés. Par défaut `true`.
	- `hopper_minecarts` : booléen. Si `false`, les minecarts avec entonnoir de la sélection ne seront pas impactés. Par défaut `true`.
	- `loot_table` : référence à une _loot table_ valide, sans l'extension `.json`. Référez-vous à la documentation des _loot tables_ de Minecraft pour savoir quoi mettre dans le fichier JSON.
	    - Si la table ne contient pas d'espace de nom (pas de `:`), elle est cherchée dans le sous-dossier `loot_tables` du dossier de la définition de la carte (voir introduction). Lors de l'application de ce traitement, la table sera copiée dans le dossier du monde sous l'espace de noms `bos` et sera référencée dans les données NBT des coffres trouvés. Si une _loot table_ existe déjà avec le même nom dans le même espace de noms, elle sera écrasée.
	    - Si la référence de la table contient un espace de nom (par exemple `minecraft:chests/nether_bridge`), alors elle sera utilisée directement sans aucune copie.
	    - Note : en cas d'utilisation de `/bos generatesphere` pour générer une sphère dans un monde existant, si la table n'a pas été ajoutée au monde avant le dernier redémarrage et si elle est dans un sous-dossier (ne me demandez pas pourquoi), alors Minecraft ne la reconnaîtrera pas et les coffres resteront vide à l'ouverture. C'est en réalité un faux problème car il suffit de redémarrer (sans avoir ouvert les coffres !) le serveur une fois pour que les coffres soient remplis. Ce problème n'existe pas lors de la génération d'une nouvelle carte.

- **`randomSpawner`**  
  Remplace tous les spawners de la sélection par des spawners d'un certain type.
	- `choices` : Liste des types d'entité sélectionnés aléatoirement. Le choix est fait aléatoirement pour chaque spawner.
