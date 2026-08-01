# Explore Screen Carousel Integration

Transform the "Featured Insights" and "Learning Collections" sections into full-width carousels to ensure a clean, one-card-at-a-time focus.

## User Review Required

> [!NOTE]
> I am replacing the `LazyRow` components with `HorizontalPager`. This will provide a "snapping" behavior where only one card is centered at a time, removing the "one and a half card" look.

## Proposed Changes

### UI Components

#### [MODIFY] [CategoryListScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/categories/CategoryListScreen.kt)
- **Featured Insights**:
    - Replace `LazyRow` with `HorizontalPager`.
    - Set `pageSpacing = 16.dp`.
    - Adjust `FeaturedFactCard` to `fillMaxWidth()`.
- **Learning Collections**:
    - Replace `LazyRow` with `HorizontalPager`.
    - Set `pageSpacing = 16.dp`.
    - Adjust `CollectionCard` to `fillMaxWidth()`.
- **Styling**:
    - Use `contentPadding = PaddingValues(horizontal = 24.dp)` in the pagers to allow a hint of the next/previous cards while keeping the current one dominant.

## Verification Plan

### Manual Verification
- **Explore Hub**: Verify that scrolling through Featured Insights and Collections snaps one card at a time.
- **Card Width**: Confirm cards now take up the majority of the screen width, looking more intentional.
- **Smoothness**: Ensure the horizontal swipe gesture feels natural and professional.
