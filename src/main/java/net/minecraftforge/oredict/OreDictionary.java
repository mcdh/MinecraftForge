package net.minecraftforge.oredict;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.ArrayUtils;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.cliffc.high_scale_lib.NonBlockingHashSet;

import java.util.*;
import java.util.stream.Collectors;

public class OreDictionary {
 public static class OreRegisterEvent extends Event {
  public final String Name;
  public final ItemStack Ore;

  public OreRegisterEvent(String name, ItemStack ore) {
   this.Name = name;
   this.Ore = ore;
  }
 }

 private static int getItemStackHash(final ItemStack ore) {
  String registryName = ore.getItem().delegate.name();
  int hash;
  if (registryName == null) {
   hash = -1;
  } else {
   hash = GameData.getItemRegistry().getId(registryName);
  }
  if (ore.getItemDamage() != WILDCARD_VALUE) {
   hash |= ((ore.getItemDamage() + 1) << 16);
  }
  return hash;
 }

 public static final class OreDictionaryEntry {
  //lookup maps for quick access
  //entry ids to their entries
  private static final Map<Integer, Set<OreDictionaryEntry>> stackHashLookup = new NonBlockingHashMap<>();
  //entry names to entry
  private static final Map<Integer, OreDictionaryEntry> idLookup = new NonBlockingHashMap<>();
  //ItemStack hashes to their encapsulating entry
  private static final Map<String, OreDictionaryEntry> nameLookup = new NonBlockingHashMap<>();

  private final String name;
  //custom set ids only, by default use hashCode
  private final Integer id;
  private final Set<ItemStack> stacks = new NonBlockingHashSet<>();

  public OreDictionaryEntry(final String name) {
   this.name = name;
   this.id = null;
   registerEntry(this);
  }

  public OreDictionaryEntry(final int id) {
   this.name = "Filler: " + id;
   this.id = id;
   registerEntry(this);
  }

  private static void registerEntry(final OreDictionaryEntry entry) {
   idLookup.put(entry.hashCode(), entry);
   nameLookup.put(entry.name, entry);
  }

  @Override
  public int hashCode() {
   if (id != null) {
    return id;
   }
   return super.hashCode();
  }

  public String getName() {
   return name;
  }

  public int getId() {
   return hashCode();
  }

  public ArrayList<ItemStack> getStacks() {
   return new SpecialArrayList<>(stacks);
  }

  public void addStack(final ItemStack stack) {
   if (stacks.add(stack.copy())) {
    final int hash = getItemStackHash(stack);
    Set<OreDictionaryEntry> entries = stackHashLookup.get(hash);
    if (entries == null) {
     entries = new NonBlockingHashSet<>();
     stackHashLookup.put(hash, entries);
    }
    entries.add(this);
   }
  }

  public void removeStack(final ItemStack stack) {
   final int hash = getItemStackHash(stack);
   final Optional<ItemStack> toRemove = stacks
    .parallelStream()
    .filter(s -> getItemStackHash(s) == hash)
    .findAny();
   //Remove associations
   if (toRemove.isPresent()) {
    stacks.remove(toRemove.get());
    stackHashLookup.get(hash).remove(this);
   }
  }
 }

 /**Minecraft changed from -1 to Short.MAX_VALUE in 1.5 release for the "block wildcard". Use this in case it changes
  * again.
  */
 public static final int WILDCARD_VALUE = Short.MAX_VALUE;
 private static boolean hasInit = false;
 public static final ArrayList<ItemStack> EMPTY_LIST = new SpecialArrayList<>(new ArrayList<>(0));
// public static final Set<OreDictionaryEntry> entries = new NonBlockingHashSet<>();

 public static boolean itemMatches(ItemStack target, ItemStack input, boolean strict) {
  if (input == null && target != null || input != null && target == null) {
   return false;
  }
  return (target.getItem() == input.getItem()
   && ((target.getItemDamage() == WILDCARD_VALUE && !strict) || target.getItemDamage() == input.getItemDamage())
  );
 }

 private static boolean containsMatch(final boolean strict, final ItemStack[] inputs, final ItemStack... targets) {
  return Arrays
   .stream(inputs)
   .parallel()
   .anyMatch(i1 -> Arrays
    .stream(targets)
    .anyMatch(i2 -> itemMatches(i1, i2, strict))
   );
 }

 private static boolean containsMatch(final boolean strict, final List<ItemStack> inputs, final ItemStack... targets) {
  return containsMatch(strict, inputs.toArray(new ItemStack[0]), targets);
 }

 @SuppressWarnings("unchecked")
 public static void initVanillaEntries() {
  if (!hasInit) {
   registerOre("logWood", new ItemStack(Blocks.log, 1, WILDCARD_VALUE));
   registerOre("logWood", new ItemStack(Blocks.log2, 1, WILDCARD_VALUE));
   registerOre("plankWood", new ItemStack(Blocks.planks, 1, WILDCARD_VALUE));
   registerOre("slabWood", new ItemStack(Blocks.wooden_slab, 1, WILDCARD_VALUE));
   registerOre("stairWood", Blocks.oak_stairs);
   registerOre("stairWood", Blocks.spruce_stairs);
   registerOre("stairWood", Blocks.birch_stairs);
   registerOre("stairWood", Blocks.jungle_stairs);
   registerOre("stairWood", Blocks.acacia_stairs);
   registerOre("stairWood", Blocks.dark_oak_stairs);
   registerOre("stickWood", Items.stick);
   registerOre("treeSapling", new ItemStack(Blocks.sapling, 1, WILDCARD_VALUE));
   registerOre("treeLeaves", new ItemStack(Blocks.leaves, 1, WILDCARD_VALUE));
   registerOre("treeLeaves", new ItemStack(Blocks.leaves2, 1, WILDCARD_VALUE));
   registerOre("oreGold", Blocks.gold_ore);
   registerOre("oreIron", Blocks.iron_ore);
   registerOre("oreLapis", Blocks.lapis_ore);
   registerOre("oreDiamond", Blocks.diamond_ore);
   registerOre("oreRedstone", Blocks.redstone_ore);
   registerOre("oreEmerald", Blocks.emerald_ore);
   registerOre("oreQuartz", Blocks.quartz_ore);
   registerOre("oreCoal", Blocks.coal_ore);
   registerOre("blockGold", Blocks.gold_block);
   registerOre("blockIron", Blocks.iron_block);
   registerOre("blockLapis", Blocks.lapis_block);
   registerOre("blockDiamond", Blocks.diamond_block);
   registerOre("blockRedstone", Blocks.redstone_block);
   registerOre("blockEmerald", Blocks.emerald_block);
   registerOre("blockQuartz", Blocks.quartz_block);
   registerOre("blockCoal", Blocks.coal_block);
   registerOre("blockGlassColorless", Blocks.glass);
   registerOre("blockGlass", Blocks.glass);
   registerOre("blockGlass", new ItemStack(Blocks.stained_glass, 1, WILDCARD_VALUE));
   //blockGlass{Color} is added below with dyes
   registerOre("paneGlassColorless", Blocks.glass_pane);
   registerOre("paneGlass", Blocks.glass_pane);
   registerOre("paneGlass", new ItemStack(Blocks.stained_glass_pane, 1, WILDCARD_VALUE));
   //paneGlass{Color} is added below with dyes
   registerOre("ingotIron", Items.iron_ingot);
   registerOre("ingotGold", Items.gold_ingot);
   registerOre("ingotBrick", Items.brick);
   registerOre("ingotBrickNether", Items.netherbrick);
   registerOre("nuggetGold", Items.gold_nugget);
   registerOre("gemDiamond", Items.diamond);
   registerOre("gemEmerald", Items.emerald);
   registerOre("gemQuartz", Items.quartz);
   registerOre("dustRedstone", Items.redstone);
   registerOre("dustGlowstone", Items.glowstone_dust);
   registerOre("gemLapis", new ItemStack(Items.dye, 1, 4));
   registerOre("slimeball", Items.slime_ball);
   registerOre("glowstone", Blocks.glowstone);
   registerOre("cropWheat", Items.wheat);
   registerOre("cropPotato", Items.potato);
   registerOre("cropCarrot", Items.carrot);
   registerOre("stone", Blocks.stone);
   registerOre("cobblestone", Blocks.cobblestone);
   registerOre("sandstone", new ItemStack(Blocks.sandstone, 1, WILDCARD_VALUE));
   registerOre("sand", new ItemStack(Blocks.sand, 1, WILDCARD_VALUE));
   registerOre("dye", new ItemStack(Items.dye, 1, WILDCARD_VALUE));
   registerOre("record", Items.record_13);
   registerOre("record", Items.record_cat);
   registerOre("record", Items.record_blocks);
   registerOre("record", Items.record_chirp);
   registerOre("record", Items.record_far);
   registerOre("record", Items.record_mall);
   registerOre("record", Items.record_mellohi);
   registerOre("record", Items.record_stal);
   registerOre("record", Items.record_strad);
   registerOre("record", Items.record_ward);
   registerOre("record", Items.record_11);
   registerOre("record", Items.record_wait);
  }

  // Build our list of items to replace with ore tags
  Map<ItemStack, String> replacements = new HashMap<ItemStack, String>();
  replacements.put(new ItemStack(Items.stick), "stickWood");
  replacements.put(new ItemStack(Blocks.planks), "plankWood");
  replacements.put(new ItemStack(Blocks.planks, 1, WILDCARD_VALUE), "plankWood");
  replacements.put(new ItemStack(Blocks.stone), "stone");
  replacements.put(new ItemStack(Blocks.stone, 1, WILDCARD_VALUE), "stone");
  replacements.put(new ItemStack(Blocks.cobblestone), "cobblestone");
  replacements.put(new ItemStack(Blocks.cobblestone, 1, WILDCARD_VALUE), "cobblestone");
  replacements.put(new ItemStack(Items.gold_ingot), "ingotGold");
  replacements.put(new ItemStack(Items.iron_ingot), "ingotIron");
  replacements.put(new ItemStack(Items.diamond), "gemDiamond");
  replacements.put(new ItemStack(Items.emerald), "gemEmerald");
  replacements.put(new ItemStack(Items.redstone), "dustRedstone");
  replacements.put(new ItemStack(Items.glowstone_dust), "dustGlowstone");
  replacements.put(new ItemStack(Blocks.glowstone), "glowstone");
  replacements.put(new ItemStack(Items.slime_ball), "slimeball");
  replacements.put(new ItemStack(Blocks.glass), "blockGlassColorless");

  // Register dyes
  String[] dyes =
   {
    "Black",
    "Red",
    "Green",
    "Brown",
    "Blue",
    "Purple",
    "Cyan",
    "LightGray",
    "Gray",
    "Pink",
    "Lime",
    "Yellow",
    "LightBlue",
    "Magenta",
    "Orange",
    "White"
   };

  for (int i = 0; i < 16; i++) {
   ItemStack dye = new ItemStack(Items.dye, 1, i);
   ItemStack block = new ItemStack(Blocks.stained_glass, 1, 15 - i);
   ItemStack pane = new ItemStack(Blocks.stained_glass_pane, 1, 15 - i);
   if (!hasInit) {
    registerOre("dye" + dyes[i], dye);
    registerOre("blockGlass" + dyes[i], block);
    registerOre("paneGlass" + dyes[i], pane);
   }
   replacements.put(dye, "dye" + dyes[i]);
   replacements.put(block, "blockGlass" + dyes[i]);
   replacements.put(pane, "paneGlass" + dyes[i]);
  }
  hasInit = true;

  ItemStack[] replaceStacks = replacements.keySet().toArray(new ItemStack[replacements.keySet().size()]);

  // Ignore recipes for the following items
  ItemStack[] exclusions = new ItemStack[]
   {
    new ItemStack(Blocks.lapis_block),
    new ItemStack(Items.cookie),
    new ItemStack(Blocks.stonebrick),
    new ItemStack(Blocks.stone_slab, 1, WILDCARD_VALUE),
    new ItemStack(Blocks.stone_stairs),
    new ItemStack(Blocks.cobblestone_wall),
    new ItemStack(Blocks.oak_stairs),
    new ItemStack(Blocks.spruce_stairs),
    new ItemStack(Blocks.birch_stairs),
    new ItemStack(Blocks.jungle_stairs),
    new ItemStack(Blocks.acacia_stairs),
    new ItemStack(Blocks.dark_oak_stairs),
    new ItemStack(Blocks.glass_pane)
   };

  List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
  List<IRecipe> recipesToRemove = new ArrayList<IRecipe>();
  List<IRecipe> recipesToAdd = new ArrayList<IRecipe>();

  // Search vanilla recipes for recipes to replace
  for (Object obj : recipes) {
   if (obj instanceof ShapedRecipes) {
    ShapedRecipes recipe = (ShapedRecipes)obj;
    ItemStack output = recipe.getRecipeOutput();
    if (output != null && containsMatch(false, exclusions, output)) {
     continue;
    }

    if (containsMatch(true, recipe.recipeItems, replaceStacks)) {
     recipesToRemove.add(recipe);
     recipesToAdd.add(new ShapedOreRecipe(recipe, replacements));
    }
   } else if (obj instanceof ShapelessRecipes) {
    ShapelessRecipes recipe = (ShapelessRecipes)obj;
    ItemStack output = recipe.getRecipeOutput();
    if (output != null && containsMatch(false, exclusions, output)) {
     continue;
    }

    if (containsMatch(true, (ItemStack[])recipe.recipeItems.toArray(new ItemStack[recipe.recipeItems.size()]), replaceStacks)) {
     recipesToRemove.add((IRecipe)obj);
     IRecipe newRecipe = new ShapelessOreRecipe(recipe, replacements);
     recipesToAdd.add(newRecipe);
    }
   }
  }

  recipes.removeAll(recipesToRemove);
  recipes.addAll(recipesToAdd);
  if (recipesToRemove.size() > 0) {
   FMLLog.info("Replaced %d ore recipies", recipesToRemove.size());
  }
 }

 static {
  //There is a deadlock bug in the STL that will cause parallel streams to deadlock if invoked from the static
  //initializer of a class. Wrapping the invocation in another thread allows the static initializer to exit and unblocks
  //the stream workers.
  //See: https://stackoverflow.com/a/34820251
  new Thread(OreDictionary::initVanillaEntries).start();
 }

 //ADDED
 public static OreDictionaryEntry findEntry(final String name) {
//  return entries
//   .parallelStream()
//   .filter(e -> name.equals(e.name))
//   .findAny()
//   .orElse(null);
  return OreDictionaryEntry.nameLookup.get(name);
 }

 /**Gets the integer ID for the specified ore name.
  * If the name does not have a ID it assigns it a new one.
  *
  * @param name The unique name for this ore 'oreIron', 'ingotIron', etc..
  * @return A number representing the ID for this ore type
  */
 public static int getOreID(final String name) {
//  int id = entries
//   .parallelStream()
//   .filter(e -> name.equals(e.name))
//   .findAny()
//   .map(Object::hashCode)
//   .orElse(-1);
//  if (id == -1) {
//   final OreDictionaryEntry entry = new OreDictionaryEntry(name);
//   entries.add(entry);
//   id = entry.hashCode();
//  }
//  return id;
  OreDictionaryEntry entry = OreDictionaryEntry.nameLookup.get(name);
  if (entry == null) {
   entry = new OreDictionaryEntry(name);
//   entries.add(entry);
  }
  return entry.getId();
 }

 /**Reverse of getOreID, will not create new entries.
  *
  * @param id The ID to translate to a string
  * @return The String name, or "Unknown" if not found.
  */
 public static String getOreName(final int id) {
//  return entries
//   .parallelStream()
//   .filter(e -> e.hashCode() == id)
//   .findAny()
//   .map(e -> e.name)
//   .orElse("Unknown");
  final OreDictionaryEntry entry = OreDictionaryEntry.idLookup.get(id);
  if (entry != null) {
   return entry.getName();
  }
  return "Unknown";
 }

 /**Gets the integer ID for the specified item stack.
  * If the item stack is not linked to any ore, this will return -1 and no new entry will be created.
  *
  * @param stack The item stack of the ore.
  * @return A number representing the ID for this ore type, or -1 if couldn't find it.
  */
 @Deprecated
 public static int getOreID(final ItemStack stack) {
  if (stack == null || stack.getItem() == null) {
   return -1;
  }
  final int hash = getItemStackHash(stack);
//  return entries
//   .parallelStream()
//   .filter(e -> e
//    .stacks
//    .stream()
//    .anyMatch(s -> getItemStackHash(s) == hash)
//   )
//   .findAny()
//   .map(Object::hashCode)
//   .orElse(-1);
  final Set<OreDictionaryEntry> entries = OreDictionaryEntry.stackHashLookup.get(hash);
  if (entries != null && !entries.isEmpty()) {
   return entries
    .stream()
    .findFirst()
    .get()
    .getId();
  }
  return -1;
 }

 /**Gets all the integer ID for the ores that the specified item stakc is registered to.
  * If the item stack is not linked to any ore, this will return an empty array and no new entry will be created.
  *
  * @param stack The item stack of the ore.
  * @return An array of ids that this ore is registerd as.
  */
 public static int[] getOreIDs(final ItemStack stack) {
  if (stack == null || stack.getItem() == null) {
   return new int[0];
  }
  final int hash = getItemStackHash(stack);
//  return ArrayUtils.toPrimitive(entries
//   .parallelStream()
//   .filter(e -> e
//    .stacks
//    .stream()
//    .anyMatch(s -> getItemStackHash(stack) == hash)
//   )
//   .map(Object::hashCode)
//   .collect(Collectors.toList())
//   .toArray(new Integer[0])
//  );
  final Set<OreDictionaryEntry> entries = OreDictionaryEntry.stackHashLookup.get(hash);
  if (entries != null && !entries.isEmpty()) {
   return ArrayUtils.toPrimitive(entries
    .parallelStream()
    .map(OreDictionaryEntry::getId)
    .collect(Collectors.toList())
    .toArray(new Integer[0])
   );
  }
  return new int[0];
 }

 /**Retrieves the ArrayList of items that are registered to this ore type.
  * Creates the list as empty if it did not exist.
  * <p>
  * The returned List is unmodifiable, but will be updated if a new ore
  * is registered using registerOre
  *
  * @param name The ore name, directly calls getOreID
  * @return An arrayList containing ItemStacks registered for this ore
  */
 public static ArrayList<ItemStack> getOres(final String name) {
//  return entries
//   .parallelStream()
//   .filter(e -> e.name.equals(name))
//   .map(e -> new SpecialArrayList<>(e.stacks))
//   .findAny()
//   .orElse(new SpecialArrayList<>(new ArrayList<>()));
  final OreDictionaryEntry entry = OreDictionaryEntry.nameLookup.get(name);
  if (entry != null) {
   return entry.getStacks();
  }
  return EMPTY_LIST;
 }

 /**Retrieves the List of items that are registered to this ore type at this instant.
  * If the flag is TRUE, then it will create the list as empty if it did not exist.
  * <p>
  * This option should be used by modders who are doing blanket scans in postInit.
  * It greatly reduces clutter in the OreDictionary is the responsible and proper
  * way to use the dictionary in a large number of cases.
  * <p>
  * The other function above is utilized in OreRecipe and is required for the
  * operation of that code.
  *
  * @param name              The ore name, directly calls getOreID if the flag is TRUE
  * @param alwaysCreateEntry Flag - should a new entry be created if empty
  * @return An arraylist containing ItemStacks registered for this ore
  */
 public static List<ItemStack> getOres(final String name, final boolean alwaysCreateEntry) {
//  OreDictionaryEntry entry = entries
//   .parallelStream()
//   .filter(e -> e.name.equals(name))
//   .findAny()
//   .orElse(null);
  OreDictionaryEntry entry = OreDictionaryEntry.nameLookup.get(name);
  if (entry == null && alwaysCreateEntry) {
//   entries.add(entry = new OreDictionaryEntry(name));
   entry = new OreDictionaryEntry(name);
  }
  if (entry != null) {
//   return new SpecialArrayList<>(entry.stacks);
   return entry.getStacks();
  }
  return EMPTY_LIST;
 }

 /**Returns whether or not an oreName exists in the dictionary.
  * This function can be used to safely query the Ore Dictionary without
  * adding needless clutter to the underlying map structure.
  * <p>
  * Please use this when possible and appropriate.
  *
  * @param name The ore name
  * @return Whether or not that name is in the Ore Dictionary.
  */
 public static boolean doesOreNameExist(final String name) {
//  return entries
//   .parallelStream()
//   .anyMatch(e -> e.name.equals(name));
  return OreDictionaryEntry.nameLookup.containsKey(name);
 }

 /**Retrieves a list of all unique ore names that are already registered.
  *
  * @return All unique ore names that are currently registered.
  */
 public static String[] getOreNames() {
//  return entries
//   .stream()
//   .map(e -> e.name)
//   .collect(Collectors.toList())
//   .toArray(new String[0]);
  return OreDictionaryEntry
   .nameLookup
   .keySet()
   .toArray(new String[0]);
 }

 /**Retrieves the ArrayList of items that are registered to this ore type.
  * Creates the list as empty if it did not exist.
  * <p>
  * Warning: In 1.8, the return value will become a immutible list,
  * and this function WILL NOT create the entry if the ID doesn't exist,
  * IDs are intended to be internal OreDictionary things and modders
  * should not ever code them in.
  *
  * @param id The ore ID, see getOreID
  * @return An List containing ItemStacks registered for this ore
  */
 @Deprecated // Use the named version not int
 public static ArrayList<ItemStack> getOres(final Integer id) {
  if (id == null) {
   throw new NullPointerException("FATAL: Tried to get the ItemStacks registered to a null ore ID!");
  }
//  final Optional<OreDictionaryEntry> optional = entries
//   .parallelStream()
//   .filter(e -> e.hashCode() == id)
//   .findAny();
//  final OreDictionaryEntry entry;
//  if (optional.isPresent()) {
//   entry = optional.get();
//  } else {
//   entry = new OreDictionaryEntry(id);
//   entries.add(entry);
//  }
//  return new SpecialArrayList<>(entry.stacks);
  OreDictionaryEntry entry = OreDictionaryEntry.idLookup.get(id);
  if (entry == null) {
   entry = new OreDictionaryEntry(id);
//   entries.add(entry);
  }
  return entry.getStacks();
 }

// private static ArrayList<ItemStack> getOres(int id) {}

 public static Collection<OreDictionaryEntry> getAllEntries() {
  return OreDictionaryEntry.idLookup.values();
 }

 public static OreDictionaryEntry getEntry(final int id) {
  return OreDictionaryEntry.idLookup.get(id);
 }

 public static OreDictionaryEntry getEntry(final String name) {
  return OreDictionaryEntry.nameLookup.get(name);
 }

 public static Collection<OreDictionaryEntry> getEntries(final ItemStack stack) {
  return OreDictionaryEntry.stackHashLookup.get(getItemStackHash(stack));
 }

 public static void registerOre(final String name, final Item ore) {
  registerOre(name, new ItemStack(ore));
 }

 public static void registerOre(final String name, final Block ore) {
  registerOre(name, new ItemStack(ore));
 }

 public static void registerOre(final String name, final ItemStack ore) {
  registerOreImpl(name, ore);
 }

 @Deprecated //Use named, not ID in 1.8+
 public static void registerOre(final int id, final Item ore) {
  registerOre(id, new ItemStack(ore));
 }

 @Deprecated //Use named, not ID in 1.8+
 public static void registerOre(final int id, final Block ore) {
  registerOre(id, new ItemStack(ore));
 }

 @Deprecated //Use named, not ID in 1.8+
 public static void registerOre(final int id, final ItemStack ore) {
  registerOreImpl(getOreName(id), ore);
 }

 public static void removeOre(final String name, final Item ore) {
  removeOre(name, new ItemStack(ore));
 }

 public static void removeOre(final String name, final Block ore) {
  removeOre(name, new ItemStack(ore));
 }

 public static void removeOre(final String name, final ItemStack ore) {
  removeOreImpl(name, ore);
 }

 private static void removeOreImpl(final String name, final ItemStack ore) {
  if (ore == null || ore.getItem() == null) {
   FMLLog.bigWarning(
    "Invalid removal attempt for an Ore Dictionary item with name %s has occurred. The registration has "
     + "been denied to prevent crashes. Contact the developer of the '%s' mod responsible for the removal needs to "
     + "correct this.",
    name,
    Loader.instance().activeModContainer().getName()
   );
  } else {
   if (!(name == null || name.isEmpty() || name.equals("Unknown"))) {
    final int hash = getItemStackHash(ore);
    if (hash == -1) {
     FMLLog.bigWarning(
      "A broken ore dictionary removal with name %s has occurred. It adds an item (type: %s) which is "
       + "currently unknown to the game registry. This dictionary item can only support a single value when removed "
       + "with ores like this, and NO I am not going to turn this spam off. Just remove your ore dictionary entries "
       + "after the GameRegistry.\nTO USERS: YES this is a BUG in the mod %s report it to them!",
      name,
      ore.getItem().getClass(),
      Loader.instance().activeModContainer().getName()
     );
    }
    OreDictionaryEntry entry = findEntry(name);
    if (entry != null) {
     entry.removeStack(ore);
    } else {
     //TODO possibly cache removal requests until met?
    }
   }
  }
 }

 private static void registerOreImpl(String name, ItemStack ore) {
  if (ore == null || ore.getItem() == null) {
   FMLLog.bigWarning(
    "Invalid removal attempt for an Ore Dictionary item with name %s has occurred. The registration has "
     + "been denied to prevent crashes. Contact the developer of the '%s' mod responsible for the registration needs "
     + "to correct this.",
    name,
    Loader.instance().activeModContainer().getName()
   );
  } else {
   if (!(name == null || name.isEmpty() || name.equals("Unknown"))) {
    final int hash = getItemStackHash(ore);
    if (hash == -1) {
     FMLLog.bigWarning(
      "A broken ore dictionary registration with name %s has occurred. It adds an item (type: %s) which is "
       + "currently unknown to the game registry. This dictionary item can only support a single value when registered "
       + "with ores like this, and NO I am not going to turn this spam off. Just register your ore dictionary entries "
       + "after the GameRegistry.\nTO USERS: YES this is a BUG in the mod %s report it to them!",
      name,
      ore.getItem().getClass(),
      Loader.instance().activeModContainer().getName()
     );
    }
    ore = ore.copy();
    OreDictionaryEntry entry = findEntry(name);
    if (entry == null) {
//     entries.add((entry = new OreDictionaryEntry(name)));
     entry = new OreDictionaryEntry(name);
//     entry.stacks.add(ore);
     entry.addStack(ore);
    } else {
//     final boolean contains = entry
//      .stacks
//      .parallelStream()
//      .anyMatch(stack -> getItemStackHash(stack) == hash);
//     if (contains) {
//      return;
//     } else {
//      entry.stacks.add(ore);
//     }
     if (OreDictionaryEntry.stackHashLookup.containsKey(hash)) {
      return;
     } else {
      entry.addStack(ore);
     }
    }
    MinecraftForge.EVENT_BUS.post(new OreRegisterEvent(name, ore));
   }
  }
 }
}
