package org.cryptocoinpartners.schema;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.cryptocoinpartners.enumeration.PersistanceAction;
import org.cryptocoinpartners.schema.dao.Dao;
import org.cryptocoinpartners.util.ConfigUtil;

/**
 * @author Tim Olson
 */
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@MappedSuperclass
public abstract class EntityBase implements Serializable, Comparable<EntityBase> {

    /**
     * 
     */
    private static final long serialVersionUID = -7893439827939854533L;
    static long delay;
    private long startTime;
    private int attempt;
    private int revision;
    private PersistanceAction persistanceAction;

    @Transient
    public long getDelay() {
        if (delay == 0)
            return ConfigUtil.combined().getInt("db.writer.delay");
        return delay;
    }

    @Transient
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    /*    @Override
        public int compareTo(Delayed o) {
            if (this.startTime < ((EntityBase) o).startTime) {
                return -1;
            }
            if (this.startTime > ((EntityBase) o).startTime) {
                return 1;
            }
            return 0;
        }*/

    @Override
    public int compareTo(EntityBase entityBase) {
        //if (this.equals(entityBase)) {
        int compareRevision = entityBase.getRevision();
        return (compareRevision - this.revision);
        //   }
        // return 1;
    }

    @Id
    @Column(columnDefinition = "BINARY(16)", length = 16, updatable = true, nullable = false)
    public UUID getId() {
        ensureId();
        return id;
    }

    @Version
    @Column(name = "version", columnDefinition = "integer DEFAULT 0", nullable = false)
    public long getVersion() {
        //  if (version == null)
        //    return 0;
        return version;
    }

    @Column(name = "revision", columnDefinition = "integer DEFAULT 0", nullable = false)
    public int getRevision() {
        //  if (version == null)
        //    return 0;
        return revision;
    }

    @Transient
    public int getAttempt() {
        //  if (version == null)
        //    return 0;
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    @Nullable
    public PersistanceAction getPeristanceAction() {
        //  if (version == null)
        //    return 0;
        return persistanceAction;
    }

    public void setPeristanceAction(PersistanceAction persistanceAction) {
        this.persistanceAction = persistanceAction;
    }

    @Transient
    public void setStartTime(long delay) {
        this.delay = delay;
        this.startTime = System.currentTimeMillis() + delay;

    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    @Override
    public String toString() {
        return "DelayedRunnable [delayMS=" + delay + ",(ms)=" + getDelay(TimeUnit.MILLISECONDS) + "]";
    }

    @Transient
    public Integer getRetryCount() {
        if (retryCount == null)
            return 0;
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public void incermentRetryCount() {
        retryCount = getRetryCount() + 1;
    }

    @Override
    public boolean equals(Object o) {
        // generated by IDEA
        if (this == o)
            return true;
        if (!(o instanceof EntityBase))
            return false;
        EntityBase that = (EntityBase) o;
        // Need to check these are not null as assinged when persisted so might not yet be present when objects are compared
        if (id == null || that.id == null)
            return false;
        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        ensureId();
        return id.hashCode();
    }

    // JPA
    protected EntityBase() {
        startTime = System.currentTimeMillis();

    }

    protected void setId(UUID id) {
        if (this.id == null)
            this.id = id;
    }

    @PostPersist
    private void postPersist() {
        //  setVersion(getVersion() + 1);
    }

    public abstract void persit();

    public abstract EntityBase refresh();

    public int findRevisionById() {
        try {
            return getDao().findRevisionById(this.getClass(), this.getId());
        } catch (NullPointerException npe) {
            return 0;
        }
    }

    public abstract void delete();

    @Transient
    public abstract Dao getDao();

    public abstract void detach();

    public abstract void merge();

    private void ensureId() {
        if (id == null)
            setId(UUID.randomUUID());
        // id = UUID.randomUUID();

        // if (startTime == 0)
        //   startTime = System.currentTimeMillis() + delay;
    }

    protected UUID id;
    protected long version;
    protected Integer retryCount;

}
