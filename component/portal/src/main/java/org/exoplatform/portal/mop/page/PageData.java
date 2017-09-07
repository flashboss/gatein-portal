package org.exoplatform.portal.mop.page;

import java.io.Serializable;
import java.util.Objects;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Utils;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;

/**
 * An immutable page data class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class PageData implements Serializable {

    /** Useful. */
    static final PageData EMPTY = new PageData();

    /** . */
    final PageKey key;

    /** . */
    final String id;

    /** . */
    final PageState state;

    private PageData() {
        this.key = null;
        this.id = null;
        this.state = null;
    }

    PageData(Page page) {
        Site site = page.getSite();

        //
        this.key = new SiteKey(Utils.siteType(site.getObjectType()), site.getName()).page(page.getName());
        this.id = page.getObjectId();
        this.state = new PageState(page);
    }

    protected Object readResolve() {
        if (key == null && state == null && id == null) {
            return EMPTY;
        } else {
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageData)) return false;
        PageData pageData = (PageData) o;
        return Objects.equals(key, pageData.key) &&
                Objects.equals(id, pageData.id) &&
                Objects.equals(state, pageData.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, id, state);
    }
}
