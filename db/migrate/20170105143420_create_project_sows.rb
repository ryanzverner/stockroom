Sequel.migration do
  up do
    create_table(:project_sows) do
      primary_key :id

      foreign_key :project_id, :projects, :foreign_key_constraint_name => 'project_sows_fkey_project_id'
      foreign_key :sow_id, :sows, :foreign_key_constraint_name => 'project_sows_fkey_sow_id'

      DateTime :updated_at
      DateTime :created_at

    end
  end

  down do
    drop_table(:project_sows)
  end
end