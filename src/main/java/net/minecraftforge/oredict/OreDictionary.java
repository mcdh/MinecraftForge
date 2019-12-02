package net.minecraftforge.oredict;

import com.google.common.collect.Lists;
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
import org.apache.logging.log4j.Level;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.cliffc.high_scale_lib.NonBlockingHashSet;

import java.util.*;
import java.util.stream.Collectors;

public class OreDictionary {
 //Forge event
 public static class OreRegisterEvent extends Event {
  public final String Name;
  public final ItemStack Ore;

  public OreRegisterEvent(String name, ItemStack ore) {
   this.Name = name;
   this.Ore = ore;
  }
 }

 /**
  * Minecraft changed from -1 to Short.MAX_VALUE in 1.5 release for the "block wildcard". Use this in case it
  * changes again.
  */
 public static final int WILDCARD_VALUE = Short.MAX_VALUE;
 public static final ArrayList<ItemStack> EMPTY_LIST = new UnmodifiableArrayList(Lists.newArrayList());

 private static boolean
  hasInit = false,
  solidified = false,
 //TODO caching
 usingCachedValues = false;

 //TODO might be able to just use finalizedIDs.keySet() and avoid an extraneous Set
 private static final NonBlockingHashSet<String> oreDictEntries;
 private static final NonBlockingHashMap<String, Set<ItemStack>> stacks;
 private static final NonBlockingHashMap<Integer, String> uniqueHashes;
 private static final NonBlockingHashMap<String, String> childMappings;
 private static final NonBlockingHashMap<String, Integer> finalizedIDs;

 static {
  oreDictEntries = new NonBlockingHashSet<>();
  uniqueHashes = new NonBlockingHashMap<>();
  stacks = new NonBlockingHashMap<>();
  childMappings = new NonBlockingHashMap<>();
  finalizedIDs = new NonBlockingHashMap<>();
  //TODO Load ore dictionary from cache if modlist matches last launch
  //TODO Check config hashes
  final Loader loader = Loader.instance();
  loader.getActiveModList();
  initVanillaEntries();
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

 public static int getOreID(final String name) {
  if (solidified) {
   Integer id = finalizedIDs.get(name);
   if (id == null) {
    FMLLog.bigWarning(
     "Requested the OreID, '%s', of a non-existent OreDictionary entry, forcing the creation of a new entry after solidifying!",
     name
    );
    rebakeMap();
    id = getOreID(name);
   }
   return id;
  }
  return -1;
//  if (!solidified) {
////   FMLLog.bigWarning(
////    "Attempted to resolve OreDictionary ID for entry '%s' before solidifying!",
////    name
////   );
//  } else {
//   Integer id = finalizedIDs.get(name);
//   if (id == null) {
//    if (solidified) {
//     FMLLog.bigWarning(
//       "Requested the OreID, '%s', of a non-existent OreDictionary entry, forcing the creation of a new entry, after solidifying!",
//       name
//      );
//     rebakeMap();
//     id = getOreID(name);
//    } else {
//     id = -1;
//    }
//   }
//   return id;
//  }
//  return -1;
 }

 //returns the oreDictID of the stack
 @Deprecated
 public static int getOreID(final ItemStack stack) {
  if (stack != null && stack.getItem() != null) {
   if (solidified) {
    //Can either search like this or use the unique hash
    //TODO Check performance of hash as comparison (hashes might collide, so maybe just leave it out)

    //Search implementation
//    final Optional<Integer> optional = oreDictEntries
//     .parallelStream()
//     .filter(entry -> entry.stacks.contains(stack))
//     .map(entry -> entry.dictID)
//     .findFirst();
//    if (optional.isPresent()) {
//     //this should never happen... force rebake?
//     if (optional.get() == -2) {
//      FMLLog.bigWarning("Attempted to fetch the ID of an OreDictionary entry, which resulted in -2. Forcing a rebake!");
//      rebakeMap();
//      return getOreID(stack);
//     }
//     return optional.get();
//    }

//    Hash implementation
    final String registryName = stack.getItem().delegate.name();
    if (registryName != null) {
     final int gameRegistryID = GameData.getItemRegistry().getId(registryName);
     //Metadata specific hash
     final String entry = uniqueHashes.get(gameRegistryID | ((stack.getItemDamage() + 1) << 16));
     if (entry != null) {
      return finalizedIDs.get(entry);
     }
    }
   }
   FMLLog.log(
    Level.DEBUG,
    "Attempted to find the oreIDs for an unregistered object (%s). This won't work very well.",
    stack
   );
   return -1;
  } else {
   FMLLog.bigWarning("Attempted to resolve OreDictionary ID for null ItemStack!");
   return -1;
  }
//  if (solidified) {
//   if (!(stack == null || stack.getItem() == null)) {
//    //Can either search like this or use the unique hash
//    //TODO Check performance of hash as comparison (hashes might collide, so maybe just leave it out)
//
//    //Search implementation
////    final Optional<Integer> optional = oreDictEntries
////     .parallelStream()
////     .filter(entry -> entry.stacks.contains(stack))
////     .map(entry -> entry.dictID)
////     .findFirst();
////    if (optional.isPresent()) {
////     //this should never happen... force rebake?
////     if (optional.get() == -2) {
////      FMLLog.bigWarning("Attempted to fetch the ID of an OreDictionary entry, which resulted in -2. Forcing a rebake!");
////      rebakeMap();
////      return getOreID(stack);
////     }
////     return optional.get();
////    }
//
////    Hash implementation
//    final String registryName = stack.getItem().delegate.name();
//    if (registryName != null) {
//     final int gameRegistryID = GameData.getItemRegistry().getId(registryName);
//     //Metadata specific hash
//     final String entry = uniqueHashes.get(gameRegistryID | ((stack.getItemDamage() + 1) << 16));
//     if (entry != null) {
//      return finalizedIDs.get(entry);
//     }
//    }
//
//   }
//  } else {
////   FMLLog.bigWarning(
////    "Attempted to resolve OreDictionary ID for ItemStack '%s' before solidifying!",
////    stack.toString()
////   );
//  }
//  FMLLog.log(
//   Level.DEBUG,
//   "Attempted to find the oreIDs for an unregistered object (%s). This won't work very well.",
//   stack
//  );
//  return -1;
 }

 public static int[] getOreIDs(final ItemStack stack) {
  if (stack != null && stack.getItem() != null) {
   if (solidified) {
    final boolean[] rebakeRequired = new boolean[] { false };
    final List<Integer> lIDs = oreDictEntries
     .parallelStream()
     .filter(entry -> stacks.get(entry).contains(stack))
     .map(finalizedIDs::get)
     .peek(id -> rebakeRequired[0] |= (id == -1))
     .collect(Collectors.toList());
    if (rebakeRequired[0]) {
     FMLLog.bigWarning(
      "Attempted to fetch the ID of an OreDictionary entry, which resulted in -2. Forcing a rebake!"
     );
     rebakeMap();
     return getOreIDs(stack);
    } else {
     final int[] ids = new int[lIDs.size()];
     for (int i = 0; i < ids.length; i++) {
      ids[i] = lIDs.get(i);
     }
     return ids;
    }
   }
   FMLLog.log(
    Level.DEBUG,
    "Attempted to find the oreIDs for an unregistered object (%s). This won't work very well.",
    stack
   );
   return new int[0];
  } else {
   FMLLog.bigWarning("Attempted to resolve OreDictionary ID for null ItemStack!");
   return new int[0];
  }
//  if (solidified) {
////   if (!(stack == null || stack.getItem() == null)) {
//   if (!(stack == null && stack.getItem() == null)) {
//    final boolean[] rebakeRequired = new boolean[] { false };
//    final List<Integer> lIDs = oreDictEntries
//     .parallelStream()
//     .filter(entry -> stacks.get(entry).contains(stack))
//     .map(entry -> finalizedIDs.get(entry))
//     .peek(id -> rebakeRequired[0] |= (id == -1))
//     .collect(Collectors.toList());
//    if (rebakeRequired[0]) {
//     FMLLog.bigWarning("Attempted to fetch the ID of an OreDictionary entry, which resulted in -2. Forcing a rebake!");
//     rebakeMap();
//     return getOreIDs(stack);
//    } else {
//     final int[] ids = new int[lIDs.size()];
//     for (int i = 0; i < ids.length; i++) {
//      ids[i] = lIDs.get(i);
//     }
//     return ids;
//    }
//   }
//  } else {
////   FMLLog.bigWarning(
////    "Attempted to resolve OreDictionary ID for ItemStack '%s' before solidifying!",
////    stack.toString()
////   );
//  }
//  FMLLog.log(Level.DEBUG, "Attempted to find the oreIDs for an unregistered object (%s). This won't work very well.", stack);
//  return new int[0];
//
 }

 public static String getOreName(final int id) {
  final String unknown = "Unknown";
  if (!solidified) {
//   FMLLog.bigWarning(
//    "Attempted to resolve OreDictionary entry by ID %d before solidifying!",
//    id
//   );
  } else {
   final boolean[] rebakeRequired = { false };
   final Optional<AbstractMap.Entry<String, Integer>> name = finalizedIDs
    .entrySet()
    .parallelStream()
    .filter(entry -> {
     rebakeRequired[0] |= entry.getValue() == -1;
     return entry.getValue() == id;
    })
    .findAny();
   if (rebakeRequired[0]) {
    rebakeMap();
    return getOreName(id);
   }
   if (name.isPresent()) {
    return name.get().getKey();
   }
  }
  return unknown;
 }

 //Addition
 public static List<String> getAllOreNames(final ItemStack stack) {
  return new SpecialArrayList<>(oreDictEntries);
 }

 public static ArrayList<ItemStack> getOres(final String name) {
  final Set<ItemStack> entry = stacks.get(name);
  if (entry == null) {
   return EMPTY_LIST;
  }
  return new SpecialArrayList<>(entry);
 }

 public static List<ItemStack> getOres(final String name, boolean alwaysCreateEntry) {
  if (solidified && alwaysCreateEntry) {
//   FMLLog.bigWarning(
//    "Attempted to create OreDictionary entry for '%s' after solidifying!",
//    name
//   );
  }
  if (alwaysCreateEntry) {
   return new SpecialArrayList<>(registerOreEntry(name));
  }
  return EMPTY_LIST;
 }

 @Deprecated
 public static ArrayList<ItemStack> getOres(final Integer id) {
  if (!solidified) {
//   FMLLog.bigWarning(
//    "Attempted to resolve OreDictionary entry by ID %d before solidifying!",
//    id
//   );
  }
  if (id != null) {
   Set<ItemStack> entry = stacks.get(getOreName(id));
   if (entry != null) {
    return new SpecialArrayList<>(entry);
   }
  }
  return EMPTY_LIST;
 }

// private static ArrayList<ItemStack> getOres(final int id) {
//  return null;
// }

 public static boolean doesOreNameExist(final String name) {
//  return oreDictEntries
//   .stream()
//   .map(entry -> entry.name)
//   .allMatch(name::equals);
//  return getOreEntry(name) != null;
  return oreDictEntries.contains(name);
 }

 public static String[] getOreNames() {
//  return oreDictEntries
//   .parallelStream()
//   .map(entry -> entry.name)
//   .toArray(String[]::new);
  return oreDictEntries.toArray(new String[0]);
 }

 private static boolean containsMatch(final boolean strict, final List<ItemStack> inputs, final ItemStack... targets) {
  return containsMatch(strict, inputs.toArray(new ItemStack[0]), targets);
 }

 private static boolean containsMatch(final boolean strict, final ItemStack[] inputs, final ItemStack... targets) {
  for (ItemStack input : inputs) {
   for (ItemStack target : targets) {
    if (itemMatches(target, input, strict)) {
     return true;
    }
   }
  }
  return false;
 }

 public static boolean itemMatches(final ItemStack target, final ItemStack input, boolean strict) {
  if (input == null && target != null || input != null && target == null) {
   return false;
  }
  return (target.getItem() == input.getItem() && ((target.getItemDamage() == WILDCARD_VALUE && !strict) || target.getItemDamage() == input.getItemDamage()));
 }

 public static void registerOre(final String name, final Item ore) {
  registerOre(name, new ItemStack(ore));
 }

 public static void registerOre(final String name, final Block ore) {
  registerOre(name, new ItemStack(ore));
 }

 @Deprecated
 public static void registerOre(final int id, final Item ore) {
  registerOre(id, new ItemStack(ore));
 }

 @Deprecated
 public static void registerOre(final int id, final Block ore) {
  registerOre(id, new ItemStack(ore));
 }

 @Deprecated
 public static void registerOre(final int id, final ItemStack ore) {
  registerOre(getOreName(id), ore);
 }

 //Addition
 private static Set<ItemStack> registerOreEntry(final String name) {
  Set<ItemStack> entry;
  if (oreDictEntries.contains(name)) {
   entry = stacks.get(name);
  } else {
   oreDictEntries.add(name);
   entry = new NonBlockingHashSet<>();
   stacks.put(name, entry);
   finalizedIDs.put(name, -1);
  }
  return entry;
 }

 public static void registerOre(final String name, final ItemStack ore) {
//  final long start = System.currentTimeMillis();
  if (!(name == null || name.isEmpty() || name.equals("Unknown"))) {
   if (ore == null || ore.getItem() == null) {
    FMLLog.bigWarning(
     "Invalid registration attempt for an Ore Dictionary item with name %s has occurred. The registration" +
      " has been denied to prevent crashes. The mod responsible for the registration needs to correct this.",
     name
    );
    return;
   }

   final Set<ItemStack> entryStacks = registerOreEntry(name);
   // HACK: use the registry name's ID. It is unique and it knows about substitutions. Fallback to a -1 value (what Item.getIDForItem would have returned) in the case where the registry is not aware of the item yet
   // IT should be noted that -1 will fail the gate further down, if an entry already exists with value -1 for this name. This is what is broken and being warned about.
   // APPARENTLY it's quite common to do this. OreDictionary should be considered alongside Recipes - you can't make them properly until you've registered with the game.
   String registryName = ore.getItem().delegate.name();
   int hash;
   if (registryName == null) {
    FMLLog.bigWarning("A broken ore dictionary registration with name %s has occurred. It adds an item (type: %s) which is currently unknown to the game registry. This dictionary item can only support a single value when"
     + " registered with ores like this, and NO I am not going to turn this spam off. Just register your ore dictionary entries after the GameRegistry.\n"
     + "TO USERS: YES this is a BUG in the mod " + Loader.instance().activeModContainer().getName() + " report it to them!", name, ore.getItem().getClass());
    hash = -1;
   } else {
    hash = GameData.getItemRegistry().getId(registryName);
   }
   if (ore.getItemDamage() != WILDCARD_VALUE) {
    hash |= ((ore.getItemDamage() + 1) << 16); // +1 so 0 is significant
   }

   String rootHash = uniqueHashes.get(hash);
   if (rootHash == null) {
    uniqueHashes.put(hash, name);
    rootHash = name;
   } else {
//    rootHash.addOreRegEntryChild(entryStacks);
    childMappings.put(rootHash, name);
   }

   final ItemStack copy = ore.copy();
//   rootHash.addStack(copy);
   entryStacks.add(copy);
   MinecraftForge.EVENT_BUS.post(new OreRegisterEvent(name, copy));
  }
//  System.out.printf("registerOre() took %dms!\n", (System.currentTimeMillis() - start));
 }

 //Remap all dictID values and all children maps
 public static void rebakeMap() {
  final boolean originalState = solidified;
  solidified = false;
  solidify(originalState);
 }

 //TODO use step bar on loading screen
 public static void solidify(final boolean partialBake) {
  if (solidified) {
   FMLLog.bigWarning("The OreDictionary has already been solidified!");
  } else {
   solidified = true;
   if (partialBake) {
    final int[] counter = new int[] { 0 };
    //find current highest entry value and find entries to remap
    finalizedIDs
     .entrySet()
     .stream()
     .filter(entry -> {
      if (entry.getValue() != -1) {
       counter[0] = Math.max(counter[0], entry.getValue());
       return false;
      }
      return true;
     })
     .map(Map.Entry::getKey)
     .collect(Collectors.toList())
     .forEach(name -> finalizedIDs.put(name, (counter[0] += 1)));
   } else {
    //final tracking variable for use within lambdas
    final int[] counter = new int[] { 0 };
    //reduce all OreRegistryEntry classes to actual item values
    oreDictEntries.forEach(entry -> finalizedIDs.put(entry, (counter[0] += 1)));
    //map all child entries to item values
//    oreDictEntries
//     .parallelStream()
//     .forEach(root -> root.childDictEntries.forEach(child -> root.addChildDictID(child.dictID)));

//   oreDictEntries.forEach(root -> root.childDictEntries.forEach(child -> root.addChildDictID(child.dictID)));
   }
  }
 }
}
