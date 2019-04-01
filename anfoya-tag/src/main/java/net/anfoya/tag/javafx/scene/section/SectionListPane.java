package net.anfoya.tag.javafx.scene.section;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.anfoya.java.undo.UndoService;
import net.anfoya.java.util.concurrent.ThreadPool;
import net.anfoya.java.util.concurrent.ThreadPool.PoolPriority;
import net.anfoya.javafx.scene.animation.DelayTimeline;
import net.anfoya.javafx.scene.control.ResetTextField;
import net.anfoya.tag.javafx.scene.dnd.ExtItemDropPane;
import net.anfoya.tag.javafx.scene.tag.TagDropPane;
import net.anfoya.tag.javafx.scene.tag.TagList;
import net.anfoya.tag.service.Section;
import net.anfoya.tag.service.Tag;
import net.anfoya.tag.service.TagException;
import net.anfoya.tag.service.TagService;

public class SectionListPane<S extends Section, T extends Tag> extends VBox {
	private static final Logger LOGGER = LoggerFactory.getLogger(SectionListPane.class);

	private final TagService<S, T> tagService;

	private final ResetTextField patternField;
	private final Accordion accordion;
	private final SelectedTagsPane<T> selectedTagsPane;

	private Set<S> sections;
	private Runnable selectTagCallback;
	private Runnable selectSectionCallback;
	private Runnable updateSectionCallback;

	private boolean sectionDisableWhenZero;

	private boolean lazyCount;
	private String itemPattern;

	private Task<Void> refreshTask;
	private final AtomicLong refreshTaskId;

	private DelayTimeline patternDelay;
	private final SectionDropPane<S> sectionDropPane;
	private final TagDropPane<S, T> tagDropPane;

	private final boolean showExcludeBox;

	private String initSectionName;
	private String initTagName;

	public SectionListPane(final TagService<S, T> tagService, UndoService undoService, final boolean withExcludeBox) {
		setPrefWidth(100);
		itemPattern = "";
		refreshTaskId = new AtomicLong();

		this.tagService = tagService;
		this.showExcludeBox = withExcludeBox;

		lazyCount = true;
		sectionDisableWhenZero = true;

		patternField = new ResetTextField();
		patternField.prefWidthProperty().bind(widthProperty());
		patternField.setPromptText("label search");
		patternField.textProperty().addListener((ov, o, n) -> refreshWithPatternAsync());
		getChildren().add(new HBox(5, patternField));

		accordion = new Accordion();
		accordion.getStyleClass().add("section-accordion");
		accordion.focusTraversableProperty().bind(focusTraversableProperty());
		accordion.expandedPaneProperty().addListener((ov, o, n) -> {
			if (n == null && !accordion.getPanes().isEmpty()) {
				new Timer("section-auto-expand-timer", true).schedule(new TimerTask() {
					@Override
					public void run() {
						if (accordion.expandedPaneProperty().isNull().get()
								&& !accordion.getPanes().isEmpty()) {
							accordion.setExpandedPane(accordion.getPanes().get(0));
						}
					}
				}, 500);
			}
		});
		accordion.expandedPaneProperty().addListener((ov, o, n) -> selectSectionCallback.run());

		final StackPane centerPane = new StackPane(accordion);
		centerPane.setAlignment(Pos.BOTTOM_CENTER);

		final ExtItemDropPane<T> extItemDropPane = new ExtItemDropPane<>();
		extItemDropPane.prefWidthProperty().bind(centerPane.widthProperty());

		sectionDropPane = new SectionDropPane<>(tagService, undoService);
		sectionDropPane.prefWidthProperty().bind(centerPane.widthProperty());

		tagDropPane = new TagDropPane<>(tagService, undoService);
		tagDropPane.prefWidthProperty().bind(centerPane.widthProperty());

		centerPane.setOnDragEntered(e -> {
			if (e.getDragboard().hasContent(Section.SECTION_DATA_FORMAT)
					&& !centerPane.getChildren().contains(sectionDropPane)) {
				final Section section = (Section) e.getDragboard().getContent(Section.SECTION_DATA_FORMAT);
				if (!section.isSystem()) {
					centerPane.getChildren().add(sectionDropPane);
				}
			} else if (e.getDragboard().hasContent(Tag.TAG_DATA_FORMAT)
					&& !centerPane.getChildren().contains(tagDropPane)) {
				final Tag tag = (Tag) e.getDragboard().getContent(Tag.TAG_DATA_FORMAT);
				tagDropPane.setSystem(tag.isSystem());
				centerPane.getChildren().add(tagDropPane);
			} else if (e.getDragboard().hasContent(ExtItemDropPane.ADD_TAG_DATA_FORMAT)
					&& !centerPane.getChildren().contains(extItemDropPane)) {
				centerPane.getChildren().add(extItemDropPane);
			}
		});
		centerPane.setOnDragExited(e -> {
			if (centerPane.getChildren().contains(sectionDropPane)) {
				centerPane.getChildren().remove(sectionDropPane);
			}
			if (centerPane.getChildren().contains(tagDropPane)) {
				centerPane.getChildren().remove(tagDropPane);
			}
			if (centerPane.getChildren().contains(extItemDropPane)) {
				centerPane.getChildren().remove(extItemDropPane);
			}
		});
		getChildren().add(centerPane);

		selectedTagsPane = new SelectedTagsPane<>();
		selectedTagsPane.setRemoveTagCallBack(tag -> clear(tag.getName()));
		getChildren().add(selectedTagsPane);

		VBox.setVgrow(centerPane, Priority.ALWAYS);
	}

	public void setOnUpdateTag(final Runnable callback) {
		tagDropPane.setOnUpdate(callback);
	}

	@SuppressWarnings("unchecked")
	public void clearSelection() {
		accordion.getPanes().forEach(pane -> ((SectionPane<S, T>) pane).clearSelection());
	}

	public void clear(final String tagName) {
		for(final TitledPane pane: accordion.getPanes()) {
			@SuppressWarnings("unchecked")
			final TagList<S, T> tagList = (TagList<S, T>) pane.getContent();
			tagList.clear(tagName);
		}
	}

	public void init(final String sectionName, final String tagName) {
		initSectionName = sectionName;
		initTagName = tagName;

		refreshAsync(() -> {
			for(final TitledPane tp: accordion.getPanes()) {
				@SuppressWarnings("unchecked")
				final SectionPane<Section, Tag> pane = (SectionPane<Section, Tag>) tp;
				if (pane.getSection().getName().equals(sectionName)) {
					pane.selectLight(tagName);
					break;
				}
			}
		});
	}

	public void setOnSelectTag(final Runnable callback) {
		selectTagCallback = () -> {
			unselectOthers();
			selectedTagsPane.refresh(getAllSelectedTags());
			callback.run();
		};
	}

	public void setOnUpdateSection(final Runnable callback) {
		updateSectionCallback = callback;
		sectionDropPane.setOnUpdateSection(callback);
	}

	public void updateItemCount(final Set<T> toRefresh, final String itemPattern, final boolean lazy) {
		updateItemCount(toRefresh, -1, itemPattern, lazy);
	}

	public void updateItemCount(Set<T> toRefresh, final int queryCount, final String itemPattern, final boolean lazy) {
		this.itemPattern = itemPattern;
		final String pattern = patternField.getText();
		final Set<T> includes = getIncludedTags();
		final Set<T> excludes = getExcludedTags();
		final boolean checkMode = isCheckMode();
		for(final TitledPane titledPane: accordion.getPanes()) {
			@SuppressWarnings("unchecked")
			final SectionPane<S, T> sectionPane = (SectionPane<S, T>) titledPane;
			if (!checkMode) {
				try {
					toRefresh = tagService.getTags(sectionPane.getSection());
				} catch (final TagException e) {
					LOGGER.error("get section count", e);
				}
			}
			sectionPane.updateCountAsync(queryCount, toRefresh, includes, excludes, itemPattern, pattern);
		}
	}

	public Set<T> getAllTags() {
		final Set<T> tags = new LinkedHashSet<>();
		for(final TitledPane titledPane: accordion.getPanes()) {
			@SuppressWarnings("unchecked")
			final TagList<S, T> tagList = (TagList<S, T>) titledPane.getContent();
			tags.addAll(tagList.getTags());
		}

		return Collections.unmodifiableSet(tags);
	}

	public Set<T> getAllSelectedTags() {
		final Set<T> tags = new LinkedHashSet<>();
		tags.addAll(getIncludedTags());
		tags.addAll(getExcludedTags());
		return tags;
	}

	public Set<T> getIncludedOrSelectedTags() {
		Set<T> included;
		if (isCheckMode()) {
			included = getIncludedTags();
		} else {
			included = new HashSet<>();
			final T tag = getSelectedTag();
			if (tag != null) {
				included.add(tag);
			}
		}
		return included;
	}

	private void refreshSections() {
		// delete sections
		final Set<Section> existingSections = new LinkedHashSet<>();
		for(final Iterator<TitledPane> i = accordion.getPanes().iterator(); i.hasNext();) {
			@SuppressWarnings("unchecked")
			final TagList<S, T> tagList = (TagList<S, T>) i.next().getContent();
			if (sections.contains(tagList.getSection())) {
				existingSections.add(tagList.getSection());
			} else {
				i.remove();
			}
		}

		// add new sections
		int index = 0;
		for(final S section: sections) {
			if (!existingSections.contains(section)) {
				final SectionPane<S, T> pane = new SectionPane<>(section, tagService, showExcludeBox);
				pane.focusTraversableProperty().bind(focusTraversableProperty());
				pane.setDisableWhenZero(sectionDisableWhenZero);
				pane.setLazyCount(lazyCount);
				pane.setOnSelectTag(selectTagCallback);
				pane.setOnUpdateSection(updateSectionCallback);
				accordion.getPanes().add(index, pane);
			}
			index++;
		}
	}

	private void refreshTags() {
		final String pattern = patternField.getText();
		final Set<T> includes = getIncludedTags();
		final Set<T> excludes = getExcludedTags();
		for(final TitledPane pane: accordion.getPanes()) {
			@SuppressWarnings("unchecked")
			final SectionPane<S, T> sectionPane = (SectionPane<S, T>) pane;
			sectionPane.refresh(pattern, includes, excludes, itemPattern);
		}
	}

	public synchronized void refreshAsync(final Runnable callback) {
		final long taskId = refreshTaskId.incrementAndGet();
		if (refreshTask != null && refreshTask.isRunning()) {
			refreshTask.cancel();
		}

		if (!patternField.getText().isEmpty()) {
			refreshWithPattern();
			return;
		}

		refreshTask = new Task<Void>() {
			@Override
			protected Void call() throws InterruptedException, TagException {
				sections = tagService.getSections();
				return null;
			}
		};
		refreshTask.setOnSucceeded(e -> {
			if (taskId == refreshTaskId.get()) {
				refreshSections();
				refreshTags();
				selectedTagsPane.refresh(getAllSelectedTags());
				if (callback != null) {
					callback.run();
				}
			}
		});
		refreshTask.setOnFailed(e -> LOGGER.error("get sections", e.getSource().getException()));
		ThreadPool.getDefault().submit(PoolPriority.MAX, "get sections", refreshTask);
	}

	private synchronized void refreshWithPatternAsync() {
		if (patternDelay != null) {
			patternDelay.stop();
		}

		if (patternField.getText().isEmpty()) {
			refreshAsync(() -> init(initSectionName, initTagName));
		} else {
			patternDelay = new DelayTimeline(Duration.millis(500), e -> refreshWithPattern());
			patternDelay.play();
		}
	}

	private synchronized void refreshWithPattern() {
		final SearchPane<S, T> searchPane;
		if (accordion.getPanes().size() == 1 && accordion.getPanes().get(0) instanceof SearchPane) {
			@SuppressWarnings("unchecked") final SearchPane<S, T> sp = (SearchPane<S, T>) accordion.getPanes().get(0);
			searchPane = sp;
		} else {
			searchPane = new SearchPane<>(tagService, showExcludeBox);
			searchPane.setOnSelectTag(selectTagCallback);
			searchPane.setOnUpdateSection(updateSectionCallback);

			accordion.getPanes().setAll(searchPane);
		}
		searchPane.refresh(patternField.getText(), getIncludedTags(), getExcludedTags(), itemPattern);
	}

	private Set<T> getIncludedTags() {
		final Set<T> tags = new LinkedHashSet<>();
		for(final TitledPane titledPane: accordion.getPanes()) {
			@SuppressWarnings("unchecked")
			final TagList<S, T> tagList = (TagList<S, T>) titledPane.getContent();
			tags.addAll(tagList.getIncludedTags());
		}

		return Collections.unmodifiableSet(tags);
	}

	public Set<T> getExcludedTags() {
		final Set<T> tags = new LinkedHashSet<>();
		for(final TitledPane titledPane: accordion.getPanes()) {
			@SuppressWarnings("unchecked")
			final TagList<S, T> tagList = (TagList<S, T>) titledPane.getContent();
			tags.addAll(tagList.getExcludedTags());
		}
		return Collections.unmodifiableSet(tags);
	}

	public boolean isSectionDisableWhenZero() {
		return sectionDisableWhenZero;
	}

	public void setSectionDisableWhenZero(final boolean disable) {
		this.sectionDisableWhenZero = disable;
	}

	public boolean isLazyCount() {
		return lazyCount;
	}

	public void setLazyCount(final boolean lazy) {
		this.lazyCount = lazy;
	}

	public S getSectionAt(final double x, final double y) {
		return null;
	}

	public T getSelectedTag() {
		T tag = null;
		for(final TitledPane sectionPane: accordion.getPanes()) {
			@SuppressWarnings("unchecked")
			final TagList<S, T> tagList = (TagList<S, T>) sectionPane.getContent();
			tag = tagList.getSelectedTag();
			if (tag != null) {
				break;
			}
		}

		return tag;
	}

	private boolean isCheckMode() {
		boolean checkMode = false;
		for(final TitledPane titledPane: accordion.getPanes()) {
			@SuppressWarnings("unchecked")
			final TagList<S, T> tagList = (TagList<S, T>) titledPane.getContent();
			checkMode = tagList.hasCheckedTag();
			if (checkMode) {
				break;
			}
		}
		return checkMode;
	}

	private void unselectOthers() {
		final TitledPane expanded = accordion.getExpandedPane();
		for(final TitledPane pane: accordion.getPanes()) {
			if (pane != expanded) {
				@SuppressWarnings("unchecked")
				final SectionPane<S, T> sectionPane = (SectionPane<S, T>) pane;
				sectionPane.clearSelection();
			}
		}
	}

	public void setOnSelectSection(Runnable callback) {
		selectSectionCallback = callback;
	}

	public S getSelectedSection() {
		@SuppressWarnings("unchecked")
		final SectionPane<S, T> sectionPane = (SectionPane<S, T>) accordion.getExpandedPane();
		return sectionPane == null? null: sectionPane.getSection();
	}
}
